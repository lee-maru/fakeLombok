package me.maru.processor;

import com.google.auto.service.AutoService;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.util.Set;

@SupportedAnnotationTypes("me.maru.anno.Set")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class SetProcessor extends AbstractProcessor {
    private ProcessingEnvironment processingEnvironment;
    private Trees trees;
    private TreeMaker treeMaker;
    private Names names;
    private Context context;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        JavacProcessingEnvironment javacProcessingEnvironment = (JavacProcessingEnvironment) processingEnv;
        super.init(processingEnv);
        this.processingEnvironment = processingEnv;
        this.trees = Trees.instance(processingEnv);
        this.context = javacProcessingEnvironment.getContext();
        this.treeMaker = TreeMaker.instance(context);
        this.names = Names.instance(context);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        TreePathScanner<Object, CompilationUnitTree> scanner = new TreePathScanner<Object, CompilationUnitTree>(){
            @Override
            public Trees visitClass(ClassTree classTree, CompilationUnitTree unitTree){
                JCTree.JCCompilationUnit compilationUnit = (JCTree.JCCompilationUnit) unitTree;
                if (compilationUnit.sourcefile.getKind() == JavaFileObject.Kind.SOURCE){
                    compilationUnit.accept(new TreeTranslator() {
                        @Override
                        public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                            super.visitClassDef(jcClassDecl);

                            List<JCTree> members = jcClassDecl.getMembers();

                            for(JCTree member : members){
                                if (member instanceof JCTree.JCVariableDecl){
                                    List<JCTree.JCMethodDecl> setters = createSetter((JCTree.JCVariableDecl) member);
                                    for(JCTree.JCMethodDecl setter : setters){
                                        System.out.println("setter " + setter);
                                        jcClassDecl.defs = jcClassDecl.defs.prepend(setter);
                                    }
                                }
                            }
                        }
                    });
                }
                return trees;
            }
        };

        for (final Element element : roundEnv.getElementsAnnotatedWith(me.maru.anno.Set.class)) {
            final TreePath path = trees.getPath(element);
            scanner.scan(path, path.getCompilationUnit());
        }



        return true;
    }

    public List<JCTree.JCMethodDecl> createSetter(JCTree.JCVariableDecl var){
        JCTree.JCVariableDecl param = treeMaker.Param(names.fromString("_"+var.getName().toString()), var.vartype.type, null);
        String str = var.name.toString();
        String upperVar = str.substring(0,1).toUpperCase()+str.substring(1,var.name.length());
        return List.of(
                treeMaker.MethodDef(
                        treeMaker.Modifiers(1),
                        names.fromString("set".concat(upperVar)),
                        treeMaker.TypeIdent(TypeTag.VOID),
                        List.nil(),
                        List.of(param),
                        List.nil(),
                        treeMaker.Block(0, List.of(treeMaker.Exec(treeMaker.Assign(
                                treeMaker.Ident(var),
                                treeMaker.Ident(param.name))))),
                        null));
    }
}

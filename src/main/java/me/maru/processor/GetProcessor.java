package me.maru.processor;

import com.google.auto.service.AutoService;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;
import me.maru.anno.Get;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.Set;

/**
 * SupportedAnnotationTypes 어떤 어노테이션을 위한 프로세서 인가?
 * SupportedSourceVersion jdk 지원 정보
 * AutoService(Processor.class) MAINFEST 자동생성
 */
@SupportedAnnotationTypes("me.maru.anno.Get")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class GetProcessor extends AbstractProcessor {
    private ProcessingEnvironment processingEnvironment;
    private Trees trees;
    private TreeMaker treeMaker;
    private Names names;
    private Context context;

    /**
     *             1. names 추후 메소드를 생성에서, parm or method 이름 생성을 위함.
     *             2. Treemaker Abstract Syntax Tree 를 make 하는 메소드 제공
     *             예) method 정의, parameter 값 정의 etc..
     */

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

    /**
     *  process 의 리턴값으로 어놈테이션을 처리하고 난 뒤, 다른 어노테이션이 지원되지 않도록 조정
     * @return true (이 필드, 클래스는 끝남) or false (이, 필드 클래스는 끝나지 않음)
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.out.println("process 메서드 실행");
        // TreePathScanner 모든 하위 트리노드를 방문하고, 상위 노드에 대한 경로를 유지하는 tree visitor
        TreePathScanner<Object, CompilationUnitTree> scanner = new TreePathScanner<Object, CompilationUnitTree>(){
            /**
             * CompillationUnitTree 는 소스파일에서 패키지 선언에서 부터 abstract syntax tree 를 정의함
             * ClassTree -> 클래스 , 인터페이스, enum 어노테이션을 트리노드로 선언
             * class 정의 위에 어노테이션 작성시 내부적으로 메소드 실행
             * CompilationUnitTree AST(Abstract Syntax Tree 의 최상단)
             */
            @Override
            public Trees visitClass(ClassTree classTree, CompilationUnitTree unitTree){
                    JCTree.JCCompilationUnit compilationUnit = (JCTree.JCCompilationUnit) unitTree;
                    // .java 파일인지 확인후 accept 를 통해 treeTransLator, 작성 메소드 생성
                    if (compilationUnit.sourcefile.getKind() == JavaFileObject.Kind.SOURCE){
                        compilationUnit.accept(new TreeTranslator() {
                            @Override
                            public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                                super.visitClassDef(jcClassDecl);
                                // Class 내부에 정의된 모든 member 를 싹다 가져옴.
                                List<JCTree> members = jcClassDecl.getMembers();
                                // Syntax tree 에서 모든 member 변수 get
                                for(JCTree member : members){
                                    if (member instanceof JCTree.JCVariableDecl){
                                        // member 변수에 대한 getter 메서드 생성
                                        List<JCTree.JCMethodDecl> getters = createGetter((JCTree.JCVariableDecl) member);
                                        for(JCTree.JCMethodDecl getter : getters){
                                            jcClassDecl.defs = jcClassDecl.defs.prepend(getter);
                                        }
                                    }
                                }
                            }
                        });
                    }
                    return trees;
            }
        };
        /**
         * RoundEnvironment
         * getElementsAnnotatedWith() -> @Get 의 어노테이션이 붙여져 있는 모든 element 를 불러 일으킨다.
         */
        for (final Element element : roundEnv.getElementsAnnotatedWith(Get.class)) {
            // 현재 어노테이션은 Type 이고 여기서 Class 뿐만 아니라, interface 와 enum 에도 작성이 가능하므로 class만 지정할 수 있도록
            if(element.getKind() != ElementKind.CLASS){
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@Get annotation cant be used on" + element.getSimpleName());
            }else{
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "@Get annotation Processing " + element.getSimpleName());
                final TreePath path = trees.getPath(element);
                scanner.scan(path, path.getCompilationUnit());
            }
        }

        return true;
    }

    public List<JCTree.JCMethodDecl> createGetter(JCTree.JCVariableDecl var){
        // 필드 이름 변수에 앞문자 대문자로 변경 해주기
        String str = var.name.toString();
        String upperVar = str.substring(0,1).toUpperCase()+str.substring(1,var.name.length());

        return List.of(
                /**
                 * treeMaker.Modifiers -> syntax tree node 에 접근하여 수정및 삽입하는 역할
                 * @Parm : treeMaker.Modifiers flag 1-> public , 2-> private, 0-> default
                 * @Parm : methodName & Type, return 정의
                 */
                treeMaker.MethodDef(
                        treeMaker.Modifiers(1), // public
                        names.fromString("get".concat(upperVar)), // 메서드 명
                        (JCTree.JCExpression) var.getType(), // return type
                        List.nil(),
                        List.nil(),
                        List.nil(),
                        // 식생성 this.a = a;
                        treeMaker.Block(1, List.of(treeMaker.Return((treeMaker.Ident(var.getName()))))),
                        null));
    }
}

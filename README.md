## Lombok @Getter, @Setter 직접 만들어 보자

![스크린샷 2021-02-04 22 47 55](https://user-images.githubusercontent.com/70433341/107016523-61bcd580-67e1-11eb-85e5-871adb97bfd6.png) ![스크린샷 2021-02-04 22 48 18](https://user-images.githubusercontent.com/70433341/107016574-6da89780-67e1-11eb-9db6-73cc30d6070e.png)

### Q : 왜 롬복을 만들어 보려고 하는가? 

### A : 알고 쓰고 싶어서

우리는 롬복을 정말 많이 사용하고는 한다. 하지만, 이 롬복이 실제로 어떻게 작동하는지에 대해서 아는사람은 많이 적을 거라고 예상한다. 나 자신도, 롬복은 그저 **마법** 같은 존재였을 뿐이었고, 보통 **이렇게만 사용하는구나,** 라고만 생각했다. 우연히

<iframe width="560" height="315" src="https://www.youtube.com/embed/QaGTSIUOuK4" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

**백기선님의 '[The JAVA, 코드를 조작하는 다양한 방법](https://www.inflearn.com/course/the-java-code-manipulation)' 의 소개 영상에서 롬복의 밑단에서 어떤 일이 발생하는지에 대해, 우리가 사용하는 코드 밑단에서 무슨일이 발생하는지에 대해서 강의가 오픈되었다고 한다.** 

이 강의에  끌리기 시작했고, 무작정 강의를 듣기 시작했다. 재밌게 잘들었지만, 롬복에 대한 호기심은 사라지지 않았고, 직접 롬복을 비슷하게 만들어본 사람이 있을까? 찾아봤지만, 한국에서는 찾기 어려웠고, **중국의 어느 개발자분이 롬복은 어떤 기술을 사용했습니다. 정도의 수준으로 정리한글** 들이 있었다. 이 글을 정독하면서,

[Lombok经常用，但是你知道它的原理是什么吗 ?](https://juejin.cn/post/6844904072789622792)

[Lombok经常用，但是你知道它的原理是什么吗？(二)](https://juejin.cn/post/6844904082084233223)

**많은 사람들이 이 글을 보면서 롬복에 대해서 간단하게 알 수 있으면 했고, 이런 오픈소스를 직접 개발을 해보고싶은 욕망이 있었기에 롬복을 만들어보자라는 생각이 들어 개발하게 되었다.**

참고 : 롬복은 Java OpenAPI를 사용하지 않는다. 물론 그렇기 때문에, 공식문서 자료가 많이 없고, 코드에 대한 설명이 부족할 수 있다. 참고 자료는 올려 놓고, 전체코드의 주석을 최대한 꼼꼼하게 작성했다. 또한 이 글에서는 롬복의 @Getter, @Setter 어노테이션을 직접 만들 것이다. 

---

### 개발환경

* Tool : intelliJ 2020.2.3 (2020.2.4 버전에서 일부 기능이 오류가 발생하는 이슈가 있어, 일부러 다운그레이드 시켜야만 했다.)
* Language : Java jdk 1.8.0_261 (oracle)
* ProjectName : fakeLombok
* 구현내용 : @Get(getter), @Set(setter)

### mvn porm.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>

<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>me.maru</groupId>
  <artifactId>fakeLombok</artifactId>
  <version>1.0</version>
  <name>fakeLombok</name>

  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.source>1.8</maven.compiler.source>
    <maven.compiler.target>1.8</maven.compiler.target>
    <auto-service.version>1.0-rc4</auto-service.version>
  </properties>

  <dependencies>

    <dependency>
      <groupId>com.google.auto.service</groupId>
      <artifactId>auto-service</artifactId>
      <version>${auto-service.version}</version>
      <optional>true</optional>
    </dependency>

    <dependency>
      <groupId>com.github.olivergondza</groupId>
      <artifactId>maven-jdk-tools-wrapper</artifactId>
      <version>0.1</version>
    </dependency>

  </dependencies>

</project>
```




### @Get 어노테이션 정의하기

​	@Getter 어노테이션을 정의만 하는건 간단하다. 하지만, 지금 Getter 와 같은 어노테이션을 만들 때, 사용자에게 이 어노테이션의 기능 스펙, 또는 사용법을 최소한으로 작성하긴 해야한다. 

```java
package me.maru.anno;

import java.lang.annotation.*;

/**
 * Fake lombok :
 * 1. 클래스 선언부 위로 선언할 시, 클래스 안에있는
 * 필드를 모두 인식하여, 바이트코드에서 getter 메서드를 자동생성
 * 2. @Get 메서드는 추 후 문제가 될 수 있으며, openApi 를 사용하여 개발한 것이
 * 아니라는점을 알고 사용하시길 바랍니다.
 *
 * @author  maru
 */
@Documented 
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Get {
}
```



* @Documented :  이 어노테이션의 스펙을 자바 문서화 시키는 걸 얘기를 한다. 위에 같이 주석을 문서화 시킬 수 있다. 즉, javadoc으로 api 문서를 만들 때, 어노테이션에 대한 설명을 포함할 수 있게 지정해 주는 것이다. 

* @Target : 어노테이션을 적용할 수 있는 위치를 의미한다.

  Type: Class, Interface(annotation), enum

  Field, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE, ANNOTATION_TYPE, PACKAGE, etc..

* @Retention : 어노테이션을 어디까지 가지고 사용할 것이냐 이다. 지금 만들어야할 @Get 은 컴파일하고 난 뒤에 필요가 없기 때문에 SOURCE 상에서만 유지하기로 하자.
---
### 어노테이션 프로세스

​	어노테이션 프로세서라고 함은, 내가 만든 어노테이션에 구체적인 동작 행위를 하기위해서 자바에서 제공하는 api 이다. @Get 어노테이션을 사용하기 위해서 @GetProcessor를 만들어보도록 하자. 우리는 **AbstractProcessor 를 extends 받아서 개발하자.**

#### GetProcessor 클래스 생성

```java
/**
 * SupportedAnnotationTypes 어떤 어노테이션을 위한 프로세서 인가?
 * SupportedSourceVersion jdk 지원 정보
 * AutoService(Processor.class) MAINFEST 자동생성
 */
@SupportedAnnotationTypes("me.maru.anno.Get")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class GetProcessor extends AbstractProcessor {

}
```

 * SupportedAnnotationTypes 어떤 어노테이션을 위한 프로세서 인가?
 * SupportedSourceVersion jdk 지원 정보 
 * AutoService(Processor.class) MAINFEST 자동생성

#### init 메소드

init 메서드를 오버라이딩 하여, 컴파일시 정보를 얻어야 한다. 예를들어 대표적으로 syntax tree 에 대한 정보를 얻어오는걸 근간으로 합니다. 

```java
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
```

* Names 추후 메소드를 생성하여 parm or method 이름 생성을 위함
* Treemaker : Abstact Syntax Tree 를 생성하는데 사용하게 된다. JCTree는 AST를 만들어내는 최상위 클래스 이다. 하지만 JCTree를 이용하여 new 를 사용하여 직접 생성할 수 없기에 Context를 이용해 AST 를 인식하고 [Treemaker](http://www.docjar.com/docs/api/com/sun/tools/javac/tree/TreeMaker.html) 라는 객체를 사용해야 한다는 것이다.   수정함 예) method 정의, method 의 parm 값 정의
* Trees : 어노테이션 프로스세의 process의 RoundEnvironment 가 코드의 element를 순회 하면서 받는 element의 정보들을 trees 에 넣기위에 선언



#### Process 메소드

이제 직접 AST를 수정해야 한다. annotation processor 의 비지니스 로직은 process 메서드를 통해서 이루어 진다. return 값은 boolean 으로. java compiler 가 return 값이  true 이면, 이 어노테이션을 처리했고, 다른 annotation processor 가 처리하지 않아도 된다. 라고 해준다. 

```java
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
                                // Syntax tree 에서 모든 member 변수 얻음.
                                for(JCTree member : members){
                                    if (member instanceof JCTree.JCVariableDecl){
                                        // member 변수에 대한 getter 메서드 생성.
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
```



1. @Get 어노테이션이 붙여져 있는 클래스를 찾은 후에 Syntax tree를 가져오도록 한다. 
2. tree 내부에서 element의 member 변수를 가지는 노드를 찾고 직접 메소드를 생성하고, 직접 method를 만들어 sytax tree 의 node를 만들어 준다. 



#### 간단하게 로직을 표현 (그림)
![image](https://user-images.githubusercontent.com/70433341/107035116-01d32880-67fb-11eb-9222-6d0a678127d0.png)

![image](https://user-images.githubusercontent.com/70433341/107034954-cc2e3f80-67fa-11eb-8eaa-7f917d140d25.png)

![image](https://user-images.githubusercontent.com/70433341/107034701-6b066c00-67fa-11eb-8300-00e62558c752.png)



이제 직접 만들어 본, 어노테이션을 사용해보도록 하자. 

Annotation 작성한 프로젝트에서 mvn clean install 을 해주도록 하자. autoService 의 도움으로 jar 패키징도 문제 없을거고 내가만든 프로젝트를 메이븐프로젝트에 의존성 주입해보도록 해보자. 

```xml
  <dependencies>

    <dependency>
      <groupId>me.maru</groupId>
      <artifactId>fakeLombok</artifactId>
      <version>1.0</version>
    </dependency>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter</artifactId>
      <version>RELEASE</version>
      <scope>test</scope>
    </dependency>
    
  </dependencies>
```



#### 어노테이션 직접 사용하기

```java
package org.example;

import me.maru.anno.Get;
import me.maru.anno.Set;

@Get @Set
public class Car {
    private String name = "로드스터 2";
    private String company = "테슬라";

}

//decomile .class file
  
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.example;

public class Car {
    private String name = "로드스터 2";
    private String company = "테슬라";

    public void setCompany(String _company) {
        this.company = _company;
    }

    public void setName(String _name) {
        this.name = _name;
    }

    public String getCompany() {
        return this.company;
    }

    public String getName() {
        return this.name;
    }

    public Car() {
    }
}

```



#### 테스트 코드 

```java
package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FakeLombokTest {
    // given
    Car car1 = new Car();

    @Test
    @DisplayName("getter 메소드 테스트")
    void testGetter(){

        //when
        String name = car1.getName();
        String company = car1.getCompany();

        //then
        assertThat(name).isEqualTo("로드스터 2");
        assertThat(company).isEqualTo("테슬라");
    }

    @Test
    @DisplayName("setter 메소드 테스트")
    void testSetter(){

        //when
        car1.setName("소나타");
        car1.setCompany("현대");
        String name = car1.getName();
        String company = car1.getCompany();

        //then
        assertThat(name).isEqualTo("소나타");
        assertThat(company).isEqualTo("현대");
    }
}
```



Setter 까지 만들어 놓은 모든 파일은 깃을 통해 확인 가능하십니다. 


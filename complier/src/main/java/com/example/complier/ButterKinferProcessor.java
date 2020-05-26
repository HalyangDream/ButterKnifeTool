package com.example.complier;

import com.example.annotationlib.BindView;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class ButterKinferProcessor extends AbstractProcessor {

    //输入日志信息
    private Messager messager;
    //文件写入
    private Filer filer;
    //获取类型工具
    private Types types;
    //gradle模块的名称
    private String moduleName = null;
    //Element 工具
    private Elements elementUtils;

    private static final String VIEW_CLASS_NAME = "android.view.View";
    private static final String ACTIVITY_CLASS_NAME = "android.app.Activity";
    private static final String TAG = "ButterKinferProcessor";

    /**
     * 日志输出信息
     *
     * @param msg
     */
    public void LoggerInfo(String msg) {
        messager.printMessage(Diagnostic.Kind.NOTE, TAG + " >> " + msg);
    }

    /**
     * 用于初始化
     *
     * @param processingEnv
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
        elementUtils = processingEnv.getElementUtils();
        types = processingEnv.getTypeUtils();
//        moduleName = processingEnv.getOptions().get("route_module_name");
        LoggerInfo("init");
    }


    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotation = new HashSet<>();
        annotation.add(BindView.class.getCanonicalName());
        return annotation;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        if (roundEnv.processingOver()) {
            return false;
        }
        //获取所有被@BindView注解的对象
        Set<? extends Element> bindViewElements = roundEnv.getElementsAnnotatedWith(BindView.class);
        // 把所有被注解的成员变量根据类搜索起来
        Map<TypeElement, Set<Element>> routesMap = new HashMap<>();
        //存放注解成员变量
        Set<Element> bindViews = null;
        if (bindViewElements == null || bindViewElements.size() == 0) {
            return false;
        }
        LoggerInfo("process start");
        for (Element element : bindViewElements) {
            TypeElement typeElement = (TypeElement) element.getEnclosingElement();
            Set<Element> elements = routesMap.get(typeElement);
            LoggerInfo("element " + element.getSimpleName());
            LoggerInfo("typeElement " + typeElement.getSimpleName());
            LoggerInfo("typeElement " + typeElement.getQualifiedName());
            LoggerInfo("typeElement " + typeElement.asType());
            if (elements != null) {
                elements.add(element);
            } else {
                bindViews = new HashSet<>();
                bindViews.add(element);
                routesMap.put(typeElement, bindViews);
            }
        }
        for (Map.Entry<TypeElement, Set<Element>> entry : routesMap.entrySet()) {
            writeFile(entry.getKey(), entry.getValue());
        }
        return true;
    }

    private void writeFile(TypeElement typeElement, Set<Element> routes) {
        LoggerInfo("writeFile");
        //获取Activity 和View的类型
        TypeMirror activityMirror = elementUtils.getTypeElement(ACTIVITY_CLASS_NAME).asType();
        TypeMirror viewMirror = elementUtils.getTypeElement(VIEW_CLASS_NAME).asType();

        LoggerInfo("activityMirror =" + activityMirror.toString());
        LoggerInfo("viewMirror =" + viewMirror.toString());

        //获取activity
        TypeMirror targetMirror = elementUtils.getTypeElement(typeElement.getQualifiedName()).asType();
        LoggerInfo("targetMirror =" + targetMirror.toString());

        //建造一个类
        TypeSpec.Builder classBuilder = TypeSpec.
                classBuilder(typeElement.getSimpleName().toString() + "$BindView")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        MethodSpec viewConstructor = buildConstructor(targetMirror, viewMirror, routes);
        classBuilder.addMethod(viewConstructor);

        boolean isActivity = types.isSubtype(typeElement.asType(), activityMirror);
        if (isActivity) {
            MethodSpec activityConstructor = buildConstructorActivity(targetMirror);
            classBuilder.addMethod(activityConstructor);
        }
        TypeSpec typeSpec = classBuilder.build();
        JavaFile javaFile = JavaFile.builder("com.example.butterkinftool", typeSpec).build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
            LoggerInfo("IOException");
        }
    }

    /**
     * 建立构造方法
     */
    private MethodSpec buildConstructor(TypeMirror target,
                                        TypeMirror view, Set<? extends Element> elements) {

        //javapoet的构造
        ParameterSpec sourceView = ParameterSpec.builder(TypeName.get(view), "sourceView").build();
        //javapoet的构造
        ParameterSpec targetActivity =
                ParameterSpec.builder(TypeName.get(target), "target").build();

        MethodSpec.Builder builder = MethodSpec.constructorBuilder();
        builder.addModifiers(Modifier.PUBLIC);
        builder.addParameter(targetActivity);
        builder.addParameter(sourceView);

        CodeBlock.Builder codeBuilder = CodeBlock.builder();
        codeBuilder.addStatement("if(target == null){ return; }");
        codeBuilder.addStatement("if(sourceView == null){ return; }");
        builder.addCode(codeBuilder.build());
        for (Element element : elements) {

            String fieldName = element.getSimpleName().toString();
            String fieldType = element.asType().toString();
            BindView annotation = element.getAnnotation(BindView.class);
            int resId = annotation.value();
            builder.addStatement("target.$L =($N)sourceView.findViewById($L)", fieldName, fieldType, resId);
        }
        return builder.build();
    }

    /**
     * 两个构造
     *
     * @param target
     * @return
     */
    private MethodSpec buildConstructorActivity(TypeMirror target) {
        ParameterSpec targetActivity =
                ParameterSpec.builder(TypeName.get(target), "target").build();
        MethodSpec.Builder builder = MethodSpec.constructorBuilder();
        builder.addModifiers(Modifier.PUBLIC);
        builder.addParameter(targetActivity);
        builder.addStatement("this(target,target.getWindow().getDecorView());");
        return builder.build();
    }

}

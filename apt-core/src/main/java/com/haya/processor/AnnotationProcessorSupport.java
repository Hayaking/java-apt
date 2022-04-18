package com.haya.processor;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.haya.annotation.ToJson;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@AutoService(Processor.class)
public class AnnotationProcessorSupport extends AbstractProcessor {


    protected Elements elementUtils;
    protected Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    /**
     * 返回支持的注解的类型
     *
     * @return
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>();
        set.add(ToJson.class.getCanonicalName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        List<TypeElement> types = new ImmutableList.Builder<TypeElement>()
                .addAll(ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(ToJson.class)))
                .build();
        for (TypeElement item : types) {
            try {
                CodeBlock codeBlock = CodeBlock
                        .builder()
                        .addStatement("return com.alibaba.fastjson.JSON.toJSONString(this)")
                        .build();
                MethodSpec method = MethodSpec
                        .methodBuilder("toJSONString")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(String.class)
                        .addCode(codeBlock)
                        .build();

                TypeSpec typeSpec = TypeSpec.classBuilder("$$"+getSimpleTypeName(item))
                        .addModifiers(Modifier.PUBLIC) // 定义类修饰符
                        .addMethods(ImmutableList.of(method))
                        .build();
                generateJavaFile(getPackageName(item), typeSpec);
            } catch (Exception e) {
                return true;
            }
        }
        return false;
    }

    protected TypeMirror getTypeMirror(String typeName) {
        return elementUtils.getTypeElement(typeName).asType();
    }

    protected String getSimpleTypeName(TypeElement typeElement) {
        return typeElement.getSimpleName().toString();
    }

    protected String getPackageName(TypeElement typeElement) {
        String qualifiedName = typeElement.getQualifiedName().toString();
        int index = qualifiedName.lastIndexOf(".");
        if (index == -1) {
            return "";
        }
        return qualifiedName.substring(0, index);
    }

    protected ParameterSpec processVariableElement(VariableElement element) {
        return ParameterSpec.builder(TypeName.get(element.asType()), element.getSimpleName().toString()).build();
    }

    protected void generateJavaFile(String packageName, TypeSpec typeSpec) throws IOException {
        JavaFile.builder(packageName, typeSpec).build().writeTo(filer);
    }


}

package com.alibaba.testable.generator;

import com.alibaba.testable.util.ConstPool;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Generate global n.e class code
 *
 * @author flin
 */
public class StaticNewClassGenerator {

    public String fetch() {
        return JavaFile.builder(ConstPool.SN_PKG,
            TypeSpec.classBuilder(ConstPool.SN_CLS)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(buildStaticNewMethod())
                .build())
            .build().toString();
    }

    private MethodSpec buildStaticNewMethod() {
        TypeVariableName typeVariable = TypeVariableName.get("T");
        MethodSpec.Builder builder = MethodSpec.methodBuilder(ConstPool.SN_METHOD)
            .addModifiers(Modifier.PUBLIC).addModifiers(Modifier.STATIC)
            .addTypeVariable(typeVariable)
            .varargs(true)
            .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), typeVariable), "type")
            .addParameter(ArrayTypeName.of(Object.class), "args")
            .returns(typeVariable);
        addStaticNewMethodStatement(builder);
        return builder.build();
    }

    private void addStaticNewMethodStatement(MethodSpec.Builder builder) {
        builder.addStatement("$T<$T> pts = new $T<>()", List.class, Class.class, ArrayList.class)
            .beginControlFlow("for (Object o : args)")
            .addStatement("pts.add(o.getClass())")
            .endControlFlow()
            .beginControlFlow("try")
            .addStatement("return type.getConstructor(pts.toArray(new Class[0])).newInstance(args)")
            .nextControlFlow("catch (Exception e)")
            .addStatement("return null")
            .endControlFlow();
    }

}

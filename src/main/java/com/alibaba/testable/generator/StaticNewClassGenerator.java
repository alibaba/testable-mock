package com.alibaba.testable.generator;

import com.alibaba.testable.util.ConstPool;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                .addField(buildStaticPoolField())
                .addMethod(buildStaticNewMethod())
                .build())
            .build().toString();
    }

    private FieldSpec buildStaticPoolField() {
        return FieldSpec.builder(ParameterizedTypeName.get(Map.class, Class.class, Object.class), "pool", Modifier.PUBLIC)
            .addModifiers(Modifier.STATIC)
            .initializer("new $T<>()", HashMap.class)
            .build();
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
            .beginControlFlow("if (!pool.isEmpty())")
            .beginControlFlow("try")
            .addStatement("T obj = (T)pool.get(type)")
            .beginControlFlow("if (obj != null)")
            .addStatement("return obj")
            .endControlFlow()
            .nextControlFlow("catch (Exception e)")
            .endControlFlow()
            .endControlFlow()
            .beginControlFlow("try")
            .addStatement("return type.getConstructor(pts.toArray(new Class[0])).newInstance(args)")
            .nextControlFlow("catch (Exception e)")
            .addStatement("return null")
            .endControlFlow();
    }

}

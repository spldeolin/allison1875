package type_use_annotation_test;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import javax.validation.constraints.NotEmpty;
import lombok.SneakyThrows;

/**
 * @author Deolin 2020-07-22
 */
public class Clz {

    public <@A1 S extends @A2 CharSequence> @A3 S myMethod() {
        return null;
    }

    private Collection<@NotEmpty String> strings;

    @SneakyThrows
    public static void main(String[] args) {
        Method myMethod = Clz.class.getDeclaredMethods()[0];

        Field strings = Clz.class.getDeclaredField("strings");
        AnnotatedType at = strings.getAnnotatedType();
        if (at instanceof AnnotatedParameterizedType) {
            AnnotatedType[] annotatedActualTypeArguments = ((AnnotatedParameterizedType) at)
                    .getAnnotatedActualTypeArguments(); // @NotEmpty String
            Annotation[] annotations = annotatedActualTypeArguments[0].getAnnotations();
            System.out.println(Arrays.toString(annotations));
        }


        AnnotatedType art = myMethod.getAnnotatedReturnType();
//        System.out.println(Arrays.toString(art.getAnnotations()) + " " + art.getType().getTypeName() + " -> ");
//        final boolean typeVariable = art instanceof AnnotatedTypeVariable;
//        if (typeVariable) {
//            System.out.println('<');
//        }
//        System.out.println(Arrays.toString(((AnnotatedElement) art.getType()).getAnnotations()) + " ");
//        System.out.println(art.getType().getTypeName());
//        if (typeVariable) {
//            AnnotatedTypeVariable atv = (AnnotatedTypeVariable) art;
//            AnnotatedType[] annotatedBounds = atv.getAnnotatedBounds();
//            if (annotatedBounds.length > 0) {
//                System.out.print(" extends ");
//                for (AnnotatedType aBound : annotatedBounds) {
//                    System.out.println(Arrays.toString(aBound.getAnnotations()) + " ");
//                    System.out.println(aBound.getType().getTypeName() + ", ");
//                }
//            }
//            System.out.println(">");
//        }

    }

}
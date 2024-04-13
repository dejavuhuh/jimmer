package org.babyfish.jimmer.client.generator.ts;

import org.babyfish.jimmer.client.generator.SourceWriter;
import org.babyfish.jimmer.client.generator.Render;
import org.babyfish.jimmer.client.meta.Doc;
import org.babyfish.jimmer.client.runtime.NullableType;
import org.babyfish.jimmer.client.runtime.ObjectType;
import org.babyfish.jimmer.client.runtime.Property;
import org.babyfish.jimmer.client.runtime.Type;
import org.babyfish.jimmer.client.runtime.impl.NullableTypeImpl;
import org.babyfish.jimmer.internal.GeneratedInput;

public class StaticTypeRender implements Render {

    private final String name;

    private final ObjectType type;

    private final boolean staticInput;

    public StaticTypeRender(String name, ObjectType type) {
        this.name = name;
        this.type = type;
        GeneratedInput generatedInput = type.getJavaType().getAnnotation(GeneratedInput.class);
        staticInput = generatedInput != null && !generatedInput.dynamic();
    }

    @Override
    public void export(SourceWriter writer) {
        writer
                .code("export type {")
                .code(name)
                .code("} from './")
                .code(name)
                .code("';\n");
    }

    @Override
    public void render(SourceWriter writer) {
        Doc doc = type.getDoc();
        writer.doc(doc).code("export interface ").code(name);
        if (!type.getArguments().isEmpty()) {
            writer.scope(SourceWriter.ScopeType.GENERIC, ", ", false, () -> {
                for (Type argument : type.getArguments()) {
                    writer.separator();
                    writer.typeRef(argument);
                }
            });
        }
        TypeScriptContext ctx = writer.getContext();
        writer.code(' ').scope(SourceWriter.ScopeType.OBJECT, "", true, () -> {
            for (Property property : type.getProperties().values()) {
                if (property.getDoc() != null) {
                    writer.doc(property.getDoc());
                } else if (doc != null) {
                    writer.doc(doc.getPropertyValueMap().get(property.getName()));
                }
                writer
                        .codeIf(!ctx.isMutable(), "readonly ")
                        .code(property.getName());
                boolean isNullable = property.getType() instanceof NullableType;
                if (isNullable && staticInput) {
                    writer.code(": ")
                            .typeRef(NullableTypeImpl.unwrap(property.getType()))
                            .code(" | null;\n");
                } else {
                    writer.codeIf(isNullable, '?')
                            .code(": ")
                            .typeRef(property.getType())
                            .code(";\n");
                }
            }
        });
        writer.code('\n');
    }
}

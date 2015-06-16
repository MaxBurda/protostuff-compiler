package io.protostuff.compiler.parser;

import io.protostuff.compiler.model.*;
import org.antlr.v4.runtime.ParserRuleContext;

import static io.protostuff.compiler.model.FieldModifier.OPTIONAL;
import static io.protostuff.compiler.model.FieldModifier.REPEATED;
import static io.protostuff.compiler.model.FieldModifier.REQUIRED;

/**
 * @author Kostiantyn Shchepanovskyi
 */
public class MessageParseListener extends AbstractProtoParsetListener {

    public static final String MAX = "max";

    public MessageParseListener(ProtoContext context) {
        super(context);
    }

    @Override
    public void enterMessageBlock(ProtoParser.MessageBlockContext ctx) {
        Message message = new Message();
        context.push(message);
    }

    @Override
    public void exitMessageBlock(ProtoParser.MessageBlockContext ctx) {
        Message message = context.pop(Message.class);
        MessageContainer container = context.peek(MessageContainer.class);
        String name = ctx.NAME().getText();
        message.setName(name);
        message.setSourceCodeLocation(getSourceCodeLocation(ctx));
        container.addMessage(message);
    }

    @Override
    public void enterField(ProtoParser.FieldContext ctx) {
        Field field = new Field();
        context.push(field);
    }

    @Override
    public void exitField(ProtoParser.FieldContext ctx) {
        Field field = context.pop(Field.class);
        FieldContainer fieldContainer = context.peek(FieldContainer.class);
        String name = ctx.name().getText();
        String type = ctx.typeReference().getText();
        Integer tag = Integer.decode(ctx.INTEGER_VALUE().getText());
        updateModifier(ctx.fieldModifier(), field);
        field.setName(name);
        field.setTag(tag);
        field.setTypeName(type);
        field.setSourceCodeLocation(getSourceCodeLocation(ctx));
        fieldContainer.addField(field);
    }

    @Override
    public void enterExtendBlock(ProtoParser.ExtendBlockContext ctx) {
        Extension extension = new Extension();
        context.push(extension);
    }

    @Override
    public void exitExtendBlock(ProtoParser.ExtendBlockContext ctx) {
        Extension extension = context.pop(Extension.class);
        String extendeeName = ctx.typeReference().getText();
        ExtensionContainer extensionContainer = context.peek(AbstractUserTypeContainer.class);
        extension.setExtendeeName(extendeeName);
        extension.setSourceCodeLocation(getSourceCodeLocation(ctx));
        extensionContainer.addDeclaredExtension(extension);
    }

    @Override
    public void enterGroupBlock(ProtoParser.GroupBlockContext ctx) {
        Group group = new Group();
        context.push(group);
    }

    @Override
    public void exitGroupBlock(ProtoParser.GroupBlockContext ctx) {
        Group group = context.pop(Group.class);
        group.setSourceCodeLocation(getSourceCodeLocation(ctx));
    }

    @Override
    public void exitExtensions(ProtoParser.ExtensionsContext ctx) {
        String fromString = ctx.from().getText();
        String toString = ctx.to() == null ? fromString : ctx.to().getText();
        int from = Integer.decode(fromString);
        int to;
        if (MAX.equals(toString)) {
            to = Field.MAX_TAG_VALUE;
        } else {
            to = Integer.decode(toString);
        }
        Message message = context.peek(Message.class);
        ExtensionRange range = new ExtensionRange(from, to);
        range.setSourceCodeLocation(getSourceCodeLocation(ctx));
        message.addExtensionRange(range);
    }

    private void updateModifier(ProtoParser.FieldModifierContext modifierContext, Field field) {
        if (modifierContext != null) {
            if (modifierContext.OPTIONAL() != null) {
                field.setModifier(OPTIONAL);
            } else if (modifierContext.REQUIRED() != null) {
                field.setModifier(REQUIRED);
            } else if (modifierContext.REPEATED() != null) {
                field.setModifier(REPEATED);
            } else {
                throw new IllegalStateException("not implemented");
            }
        }
    }
}
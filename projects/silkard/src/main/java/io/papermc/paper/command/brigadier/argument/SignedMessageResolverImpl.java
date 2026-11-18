package io.papermc.paper.command.brigadier.argument;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.chat.SignedMessage;
import net.minecraft.commands.CommandSourceStack;
import com.mohistmc.silkard.paper.paper.PlayerChatMessageView;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.PlayerChatMessage;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record SignedMessageResolverImpl(MessageArgument.Message message) implements SignedMessageResolver {

    @Override
    public String content() {
        return this.message.text();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public CompletableFuture<SignedMessage> resolveSignedMessage(final String argumentName, final CommandContext erased) throws CommandSyntaxException {
        final CompletableFuture<SignedMessage> future = new CompletableFuture<>();

        final MessageArgument.Message response = ((CommandContext<CommandSourceStack>) erased).getArgument(argumentName, SignedMessageResolverImpl.class).message;
        // Vanilla's resolveChatMessage reads the argument as a MessageArgument.Message, which this resolver wraps,
        // so the signed argument is looked up here; unsigned input becomes a disguised system message.
        final CommandSourceStack source = ((CommandContext<CommandSourceStack>) erased).getSource();
        final PlayerChatMessage signed = source.getSigningContext().getArgument(argumentName);
        final PlayerChatMessage message = signed != null
            ? signed
            : PlayerChatMessage.system(response.text()).withUnsignedContent(response.toComponent(source, source.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_ENTITY_SELECTORS)));
        future.complete(PlayerChatMessageView.of(message));
        return future;
    }
}

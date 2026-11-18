package com.mohistmc.silkard.paper.paper;

import io.papermc.paper.adventure.PaperAdventure;
import java.time.Instant;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.PlayerChatMessage;
import org.jspecify.annotations.Nullable;

/**
 * Adventure view of a vanilla {@link PlayerChatMessage}, standing in for the
 * {@code PlayerChatMessage.AdventureView} inner class Paper patches into the vanilla record.
 */
public record PlayerChatMessageView(PlayerChatMessage playerChatMessage) implements SignedMessage {

    public static PlayerChatMessageView of(final PlayerChatMessage message) {
        return new PlayerChatMessageView(message);
    }

    @Override
    public Instant timestamp() {
        return this.playerChatMessage.timeStamp();
    }

    @Override
    public long salt() {
        return this.playerChatMessage.salt();
    }

    @Override
    public @Nullable Signature signature() {
        final MessageSignature signature = this.playerChatMessage.signature();
        return signature == null ? null : SignedMessage.signature(signature.bytes());
    }

    @Override
    public @Nullable Component unsignedContent() {
        final net.minecraft.network.chat.Component unsigned = this.playerChatMessage.unsignedContent();
        return unsigned == null ? null : PaperAdventure.asAdventure(unsigned);
    }

    @Override
    public String message() {
        return this.playerChatMessage.signedContent();
    }

    @Override
    public Identity identity() {
        return this.playerChatMessage.isSystem() ? Identity.nil() : Identity.identity(this.playerChatMessage.sender());
    }
}

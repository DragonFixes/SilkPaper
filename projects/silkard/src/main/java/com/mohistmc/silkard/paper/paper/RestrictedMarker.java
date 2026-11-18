package com.mohistmc.silkard.paper.paper;

/**
 * Marks a command requirement predicate as restricted, standing in for the
 * {@code Commands.RestrictedMarker} interface Paper adds to vanilla's {@code Commands}.
 */
// TODO Silkard - vanilla's command tree does not read this marker, so restricted commands get no extra confirmation
public interface RestrictedMarker {

    boolean restricted();
}

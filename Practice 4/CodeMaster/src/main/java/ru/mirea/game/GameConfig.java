package ru.mirea.game;

public record GameConfig(
        int minPlayers,
        int maxPlayers,
        int codeSymbolCount,
        int roundDuration
) {

}

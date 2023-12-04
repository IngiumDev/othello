package de.lmu.bio.ifi;

public class BitMasks {
    // Longthello
    public final static long UP_MASK = -256L;
    public final static long DOWN_MASK = 72057594037927935L;
    public final static long RIGHT_MASK = 9187201950435737471L;
    public final static long LEFT_MASK = -72340172838076674L;
    // diagonal to corners towards middle
    public final static long TERRIBLE_MOVES_1 = 18577348462920192L;
    // adjacent to corners
    public final static long TERRIBLE_MOVES_2 = 4792111478498951490L;
    // middle 4 pieces that are one away from the edges
    public final static long TERRIBLE_MOVES_3 = 16961350949551104L;
    public final static long TERRIBLE_MOVES_COMBINED = 4827650177911422786L;
    public final static long DOWN_LEFT_MASK = 71775015237779198L;
    public final static long BOTTOM_LEFT_CORNER = 0x100000000000000L;
    public final static long BOTTOM_RIGHT_CORNER = 0x8000000000000000L;
    public final static long TOP_LEFT_CORNER = 0x1L;
    public final static long TOP_RIGHT_CORNER = 0x80L;

    public final static long TERRIBLE_MOVES = 0x42e742810042e742L;
    public final static long DOWN_RIGHT_MASK = 35887507618889599L;
    public final static long UP_LEFT_MASK = -72340172838076928L;
    public final static long UP_RIGHT_MASK = 9187201950435737344L;

    // Matrix Evaluation
    // = A1
    public final static long ALL_CORNER_POSITIONS = BOTTOM_LEFT_CORNER | BOTTOM_RIGHT_CORNER | TOP_LEFT_CORNER | TOP_RIGHT_CORNER;

    public final static long A1_POSITIONS = ALL_CORNER_POSITIONS;

    public final static int A1_SCORE = 101;
    public final static long B1_POSITIONS = 4755801206503243842L;
    public final static int B1_SCORE = -43;
    public final static long C1_POSITIONS = 2594073385365405732L;
    public final static int C1_SCORE = 38;
    public final static long D1_POSITIONS = 1729382256910270488L;
    public final static int D1_SCORE = 7;
    public final static long A2_POSITIONS = 36310271995707648L;
    public final static int A2_SCORE = -27;
    public final static long B2_POSITIONS = 18577348462920192L;
    public final static int B2_SCORE = -74;
    public final static long C2_POSITIONS = 10133099161592832L;
    public final static int C2_SCORE = -16;
    public final static long D2_POSITIONS = 6755399441061888L;
    public final static int D2_SCORE = -14;
    public final static long A3_POSITIONS = 141837008437248L;
    public final static int A3_SCORE = 56;
    public final static long B3_POSITIONS = 72567771758592L;
    public final static int B3_SCORE = -30;
    public final static long C3_POSITIONS = 39582420959232L;
    public final static int C3_SCORE = 12;
    public final static long D3_POSITIONS = 26388280639488L;
    public final static int D3_SCORE = 5;
    public final static long A4C4_POSITIONS = 711437844480L;
    public final static int A4C4_SCORE = 1;
    public final static long B4_POSITIONS = 284575137792L;
    public final static int B4_SCORE = -8;
    public final static long D4_POSITIONS = 103481868288L;
    public final static int D4_SCORE = -1;
    public final static int[] WEIGHT_MATRIX_SCORES = {
            A1_SCORE, B1_SCORE, C1_SCORE, D1_SCORE,
            A2_SCORE, B2_SCORE, C2_SCORE, D2_SCORE,
            A3_SCORE, B3_SCORE, C3_SCORE, D3_SCORE,
            A4C4_SCORE, B4_SCORE, D4_SCORE
    };
    public final static long[] WEIGHT_MATRIX = {
            A1_POSITIONS, B1_POSITIONS, C1_POSITIONS, D1_POSITIONS,
            A2_POSITIONS, B2_POSITIONS, C2_POSITIONS, D2_POSITIONS,
            A3_POSITIONS, B3_POSITIONS, C3_POSITIONS, D3_POSITIONS,
            A4C4_POSITIONS, B4_POSITIONS, D4_POSITIONS
    };

    public final static int[] BIT_DIRECTIONS = {-9, -8, -7, -1, 1, 7, 8, 9};
}

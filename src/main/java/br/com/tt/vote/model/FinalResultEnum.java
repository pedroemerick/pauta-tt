package br.com.tt.vote.model;

public enum FinalResultEnum {

    APPROVED("APROVADA"),
    DISAPPROVED("REPROVADA"),
    INCONCLUSIVE("INCONCLUSIVO");

    public final String label;

    FinalResultEnum(String label) {
        this.label = label;
    }
}

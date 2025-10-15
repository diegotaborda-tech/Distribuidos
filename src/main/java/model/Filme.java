package model;

import java.util.List;

public class Filme {

	private String id;
	private String nome;
	private String ano;
	private List<String> generos;
	private String sinopse;
	private Object avaliacoes;
	private String nota;
	private String qtd_avaliacoes;
	

    public Filme(String id, String nome, String ano, List<String> generos, String sinopse, Object avaliacoes, String nota, String qtd_avaliacoes) {
        this.nome = nome;
        this.ano = ano;
        this.sinopse = sinopse;
		this.generos = generos;
		this.avaliacoes = avaliacoes;
		this.id = id;
		this.nota = nota;
		this.qtd_avaliacoes = qtd_avaliacoes;
    }
    public Filme(String id, String nome, String ano, List<String> generos, String sinopse, String nota, String qtd_avaliacoes) {
        this.nome = nome;
        this.ano = ano;
        this.sinopse = sinopse;
		this.generos = generos;
		this.id = id;
		this.nota = nota;
		this.qtd_avaliacoes = qtd_avaliacoes;
    }
    public Filme(String nome, String ano, List<String> generos, String sinopse) {
        this.nome = nome;
        this.ano = ano;
        this.sinopse = sinopse;
		this.generos = generos;
    }

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getAno() {
		return ano;
	}
	public void setAno(String ano) {
		this.ano = ano;
	}
	public String getSinopse() {
		return sinopse;
	}
	public void setSinopse(String sinopse) {
		this.sinopse = sinopse;
	}

	@Override
    public String toString() {
        // Exemplo: "Interestelar (2014) - Nota: 0.0"
        return this.nome + " (" + this.ano + ") - Nota: " + this.nota;
    }
}

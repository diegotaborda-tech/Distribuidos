package model;

import java.util.List;

public class Filme {

	private String id;
	private String titulo;
	private String ano;
	private String diretor;
	private List<String> genero;
	private String sinopse;
	private Object avaliacoes;
	private String nota;
	private String qtd_avaliacoes;
	

    public Filme(String id, String titulo, String ano, List<String> generos, String sinopse, Object avaliacoes, String nota, String qtd_avaliacoes) {
        this.titulo = titulo;
        this.ano = ano;
        this.sinopse = sinopse;
		this.genero = generos;
		this.avaliacoes = avaliacoes;
		this.id = id;
		this.nota = nota;
		this.qtd_avaliacoes = qtd_avaliacoes;
		
    }
    public Filme(String id, String nome, String ano, List<String> generos, String sinopse, String nota, String qtd_avaliacoes, String diretor) {
        this.titulo = nome;
        this.ano = ano;
        this.sinopse = sinopse;
		this.genero = generos;
		this.id = id;
		this.nota = nota;
		this.qtd_avaliacoes = qtd_avaliacoes;
		this.diretor = diretor;
    }
    public Filme(String nome, String ano, List<String> generos, String sinopse) {
        this.titulo = nome;
        this.ano = ano;
        this.sinopse = sinopse;
		this.genero = generos;
    }

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTitulo() {
		return titulo;
	}
	public void setTitulo(String titulo) {
		this.titulo = titulo;
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
        return this.titulo + " (" + this.ano + ") - Nota: " + this.nota;
    }
}

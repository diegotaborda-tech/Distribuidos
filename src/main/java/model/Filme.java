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

    // --- CONSTRUTORES ---

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
    public Filme(String nome, String ano, List<String> generos, String sinopse, String diretor) {
        this.titulo = nome;
        this.ano = ano;
        this.sinopse = sinopse;
		this.genero = generos;
		this.diretor = diretor;
    }

    // --- GETTERS E SETTERS ---

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
	public String getDiretor() {
		return diretor;
	}
	public void setDiretor(String diretor) {
		this.diretor = diretor;
	}
	public List<String> getGenero() {
		return genero;
	}
	public void setGenero(List<String> genero) {
		this.genero = genero;
	}
	public String getSinopse() {
		return sinopse;
	}
	public void setSinopse(String sinopse) {
		this.sinopse = sinopse;
	}
	public Object getAvaliacoes() {
		return avaliacoes;
	}
	public void setAvaliacoes(Object avaliacoes) {
		this.avaliacoes = avaliacoes;
	}
	public String getNota() {
		return nota;
	}
	public void setNota(String nota) {
		this.nota = nota;
	}
	public String getQtd_avaliacoes() {
		return qtd_avaliacoes;
	}
	public void setQtd_avaliacoes(String qtd_avaliacoes) {
		this.qtd_avaliacoes = qtd_avaliacoes;
	}

    // --- MÉTODO toString ---

	// Dentro da classe model/Filme.java

@Override
public String toString() {
    // Exibirá: O Poderoso Chefão (1972) - Nota: 0.0
    return this.titulo + " (" + this.ano + ") - Nota: " + this.nota;
}
}
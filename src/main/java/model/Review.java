package model;

public class Review {

	private String id;
	private String id_filme;
	private String id_usuario;
	private String nota;
	private String data;
	private String descricao;
	private String titulo;

	public Review(String id, String id_filme, String id_usuario, String nota, String data, String descricao) {
		this.id = id;
		this.id_filme = id_filme;
		this.id_usuario = id_usuario;
		this.nota = nota;
		this.data = data;
		this.descricao = descricao;
	}

	public Review(String id_filme, String id_usuario, String nota, String data, String descricao) {
		this.id_filme = id_filme;
		this.id_usuario = id_usuario;
		this.nota = nota;
		this.data = data;
		this.descricao = descricao;
	}

	public Review(String id_filme, String titulo, String descricao, String nota) {
		this.id_filme = id_filme;
		this.titulo = titulo;
		this.descricao = descricao;
		this.nota = nota;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId_filme() {
		return id_filme;
	}

	public void setId_filme(String id_filme) {
		this.id_filme = id_filme;
	}

	public String getId_usuario() {
		return id_usuario;
	}

	public void setId_usuario(String id_usuario) {
		this.id_usuario = id_usuario;
	}
	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getNota() {
		return nota;
	}

	public void setNota(String nota) {
		this.nota = nota;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Override
	public String toString() {

		return "Nota: " + this.nota + " - Filme: " + this.id_filme + " - Review: " + this.descricao;
	}
}

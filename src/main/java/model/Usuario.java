package model;

public class Usuario {
    
    @Override
    public String toString() {
        // É assim que o usuário será exibido na JList
        return "ID: " + id + "  |  Nome: " + nome;
    }
    private String id;
    private String nome;
    private String senha;
    private String usuario;

    // Construtor padrão
    public Usuario() {
    	
    }

    public Usuario( String senha) {
        
    	this.senha = senha;
    }
    
    public Usuario(String nome, String senha) {
    	this.nome = nome;
    	this.senha = senha;
    }

    public Usuario(String id, String nome, String senha) {
        this.id = id;
        this.nome = nome;
        this.senha = senha;
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

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
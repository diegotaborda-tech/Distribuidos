package model;

public class Login{
    private String usuario;
    private String senha;
    private String operacao;

    public Login() {

    }

    public Login(String usuario, String senha, String operacao) {
        this.operacao = operacao;
        this.usuario = usuario;
        this.senha = senha;
    }
    
//    public Login(Object usuario,) {
//    	this.nome = usuario;
//    	this.senha = senha;
//    }
    
    public String getUsuario() {
        return usuario;
    }
    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }
    public String getSenha() {
        return senha;
    }
    public void setSenha(String senha) {
        this.senha = senha;
    }
    public String getOperacao() {
        return operacao;
    }
    public void setOperacao(String operacao) {
        this.operacao = operacao;
    }
}

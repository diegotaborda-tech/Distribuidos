package model;

public class Login{
    private String usuario;
    private String senha;
    private String status;
    private String operacao;
    private String mensagem;

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
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getOperacao() {
    	return operacao;
    }
    public void setOperacao(String operacao) {
    	this.operacao = operacao;
    }
    public String getMensagem() {
        return mensagem;
    }
    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
}

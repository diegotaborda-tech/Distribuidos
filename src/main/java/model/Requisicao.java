package model;

public class Requisicao {

    private String operacao;
    private String token;
    private Object usuario;
    private Object review;
    private Object filme;
    private String id;



    public Requisicao(String operacao) {
        this.operacao = operacao;
        this.token = null; 
    }
    public Requisicao(String operacao, String token) {
        this.operacao = operacao;
        this.token = token; 
    }
    
    // Construtor para operações com ID (como EXCLUIR_FILME)
    public Requisicao(String operacao, String token, String id) {
        this.operacao = operacao;
        this.token = token;
        this.id = id;
    }
    
    //User
    public Requisicao(String operacao, Object dados,String token,String tipo) {
        
        this.operacao = operacao;
        this.token = token;

        if (tipo.equals("usuario")) {
            this.usuario = dados;
        } else if (tipo.equals("review")) {
            this.review = dados;
        } else if (tipo.equals("filme")) {
            this.filme = dados;
        }
    }
    //User
    public Requisicao(String operacao, Object dados, String tipo) {
        this.operacao = operacao;
        
        if (tipo.equals("usuario")) {
            this.usuario = dados;
        } else if (tipo.equals("review")) {
            this.review = dados;
        } else if (tipo.equals("filme")) {
            this.filme = dados;
        }
    }

        public String getOperacao() {
            return operacao;
        }

        public void setOperacao(String operacao) {
            this.operacao = operacao;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public void setUsuario(Object usuario) {
            this.usuario = usuario;
        }

        public Object getUsuario() {
            return usuario;
        }

        public void setFilme(Object filme) {
            this.filme = filme;
        }

        public Object getFilme() {
            return filme;
        }

        public void setReview(Object review) {
            this.review = review;
        }

        public Object getReview() {
            return review;
        }
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
}

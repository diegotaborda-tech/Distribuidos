package model;

import java.util.List;
import java.util.ArrayList;

public class Lista_Usuarios {
    
    // 1. Declara a lista como um atributo privado.
    // Usamos a interface List e o tipo <Usuario> para especificar
    // que a lista conterá objetos da classe Usuario.
    private List<Usuario> usuarios;

    // 2. Crie um construtor para inicializar a lista.
    // É uma boa prática inicializar a lista no construtor para
    // garantir que ela nunca seja nula.
    public Lista_Usuarios() {
        this.usuarios = new ArrayList<>();
    }
    
    // 3. Crie um método para adicionar um novo usuário à lista.
    public void adicionarUsuario(Usuario usuario) {
        this.usuarios.add(usuario);
    }
    
    // 4. Crie um método para obter a lista inteira.
    // Isso é útil para acessar todos os usuários.
    public List<Usuario> getUsuarios() {
        return this.usuarios;
    }
    
    // 5. Crie um método para obter um usuário por índice (opcional).
    // public Usuario getUsuario(int indice) {
    //     if (indice >= 0 && indice < this.usuarios.size()) {
    //         return this.usuarios.get(indice);
    //     }
    //     return null; // Retorna nulo se o índice for inválido
    // }
}
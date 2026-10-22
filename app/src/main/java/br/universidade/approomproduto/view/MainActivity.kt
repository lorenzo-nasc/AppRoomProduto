package br.universidade.approomproduto.view

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import br.universidade.approomproduto.dao.ProductDao
import br.universidade.approomproduto.databinding.ActivityMainBinding
import br.universidade.approomproduto.db.AppDatabase
import br.universidade.approomproduto.model.Product
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var productDao: ProductDao
    private val moeda = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        productDao = AppDatabase.getInstance(applicationContext).productDao()

        binding.btnInserirProduto.setOnClickListener { inserirProduto() }
        binding.btnListarProdutos.setOnClickListener { listarProdutos() }
        binding.btnBuscar.setOnClickListener { buscarProdutos() }
        binding.btnAtualizarPreco.setOnClickListener { atualizarPreco() }
        binding.btnExcluirTodos.setOnClickListener { confirmarExclusaoTotal() }
        binding.btnExcluirId.setOnClickListener { excluirPorId() }

        // Carrega produtos persistidos ao abrir novamente o aplicativo.
        listarProdutos()
    }

    // A1, A2 e A8: cadastro, validação, quantidade e limpeza dos campos.
    private fun inserirProduto() {
        val nome = binding.editProductName.text.toString().trim()
        val preco = converterPreco(binding.editProductPrice.text.toString())
        val quantidade = binding.editProductQuantity.text.toString().trim().toIntOrNull()
        val descricao = binding.editProductDescription.text.toString().trim().ifBlank { null }

        if (nome.isBlank()) {
            binding.editProductName.error = "Informe o nome do produto"
            binding.editProductName.requestFocus()
            return
        }
        if (preco == null || !preco.isFinite() || preco <= 0.0) {
            binding.editProductPrice.error = "Informe um preço válido"
            binding.editProductPrice.requestFocus()
            return
        }
        if (quantidade == null || quantidade < 0) {
            binding.editProductQuantity.error = "Informe uma quantidade válida (0 ou maior)"
            binding.editProductQuantity.requestFocus()
            return
        }

        lifecycleScope.launch {
            productDao.insertAll(
                Product(name = nome, price = preco, description = descricao, quantity = quantidade)
            )
            Log.i("TAG", "Produto inserido: $nome - ${moeda.format(preco)}")
            Toast.makeText(this@MainActivity, "Produto inserido!", Toast.LENGTH_SHORT).show()

            binding.editProductName.text?.clear()
            binding.editProductPrice.text?.clear()
            binding.editProductDescription.text?.clear()
            binding.editProductQuantity.text?.clear()
            binding.editProductName.requestFocus()

            listarProdutos()
        }
    }

    // A3 e A4: apresentação dentro de um TextView e totais globais.
    private fun listarProdutos() {
        lifecycleScope.launch {
            val produtos = productDao.getAll()
            mostrarProdutos(produtos, produtos, "Lista completa")
        }
    }

    // A5: busca parcial (LIKE com porcentagens nos dois lados).
    private fun buscarProdutos() {
        val texto = binding.editSearchName.text.toString().trim()
        if (texto.isEmpty()) {
            binding.editSearchName.error = "Digite um nome para buscar"
            return
        }
        lifecycleScope.launch {
            val encontrados = productDao.buscarPorNome("%$texto%")
            val todos = productDao.getAll()
            mostrarProdutos(encontrados, todos, "Busca por: $texto")
        }
    }

    // A7: mantém o ID e os demais atributos; muda somente o preço.
    private fun atualizarPreco() {
        val id = binding.editUpdateId.text.toString().trim().toIntOrNull()
        val novoPreco = converterPreco(binding.editUpdatePrice.text.toString())

        if (id == null || id <= 0) {
            binding.editUpdateId.error = "Informe um ID válido"
            return
        }
        if (novoPreco == null || !novoPreco.isFinite() || novoPreco <= 0.0) {
            binding.editUpdatePrice.error = "Informe um preço válido"
            return
        }

        lifecycleScope.launch {
            val atual = productDao.buscarPorId(id)
            if (atual == null) {
                Toast.makeText(this@MainActivity, "Produto com ID $id não encontrado", Toast.LENGTH_SHORT).show()
                return@launch
            }
            productDao.update(atual.copy(price = novoPreco))
            Toast.makeText(this@MainActivity, "Preço atualizado!", Toast.LENGTH_SHORT).show()
            binding.editUpdateId.text?.clear()
            binding.editUpdatePrice.text?.clear()
            listarProdutos()
        }
    }

    // A6: confirmação antes de apagar o banco inteiro.
    private fun confirmarExclusaoTotal() {
        AlertDialog.Builder(this)
            .setTitle("Excluir todos os produtos?")
            .setMessage("Esta ação removerá os produtos cadastrados e não poderá ser desfeita.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Excluir") { _, _ ->
                lifecycleScope.launch {
                    productDao.deleteAll()
                    Toast.makeText(this@MainActivity, "Produtos excluídos", Toast.LENGTH_SHORT).show()
                    listarProdutos()
                }
            }
            .show()
    }

    // Desafio extra da A6: excluir apenas um registro usando @Delete.
    private fun excluirPorId() {
        val id = binding.editDeleteId.text.toString().trim().toIntOrNull()
        if (id == null || id <= 0) {
            binding.editDeleteId.error = "Informe um ID válido"
            return
        }
        lifecycleScope.launch {
            val produto = productDao.buscarPorId(id)
            if (produto == null) {
                Toast.makeText(this@MainActivity, "Produto com ID $id não encontrado", Toast.LENGTH_SHORT).show()
                return@launch
            }
            productDao.delete(produto)
            Toast.makeText(this@MainActivity, "Produto excluído!", Toast.LENGTH_SHORT).show()
            binding.editDeleteId.text?.clear()
            listarProdutos()
        }
    }

    // Converte preço decimal digitado com vírgula (Brasil) ou ponto.
    private fun converterPreco(texto: String): Double? =
        texto.trim().replace(',', '.').toDoubleOrNull()

    private fun mostrarProdutos(
        exibidos: List<Product>,
        todos: List<Product>,
        titulo: String
    ) {
        val totalProdutos = todos.size
        val somaDosPrecos = todos.sumOf { it.price }
        val unidades = todos.sumOf { it.quantity }
        val valorEmEstoque = todos.sumOf { it.price * it.quantity }

        binding.txtResumoEstoque.text =
            "Produtos cadastrados: $totalProdutos\n" +
            "Soma dos preços: ${moeda.format(somaDosPrecos)}\n" +
            "Unidades em estoque: $unidades\n" +
            "Valor estimado do estoque: ${moeda.format(valorEmEstoque)}"

        binding.txtListaProdutos.text = if (exibidos.isEmpty()) {
            "$titulo (0 resultados)\n\nNenhum produto encontrado."
        } else {
            "$titulo (${exibidos.size} resultados)\n\n" +
                exibidos.joinToString("\n\n") { produto ->
                    "ID: ${produto.id}  •  ${produto.name}\n" +
                    "Preço: ${moeda.format(produto.price)}  •  Quantidade: ${produto.quantity}\n" +
                    "Descrição: ${produto.description ?: "(sem descrição)"}"
                }
        }

        exibidos.forEach { produto ->
            Log.i(
                "TAG",
                "Produto = ${produto.name}, Preço = ${moeda.format(produto.price)}, " +
                    "Quantidade = ${produto.quantity}, Descrição = ${produto.description}"
            )
        }
        if (exibidos.isEmpty()) Log.i("TAG", "Nenhum produto encontrado.")
    }
}

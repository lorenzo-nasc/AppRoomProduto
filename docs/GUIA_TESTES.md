# Guia de teste e capturas — AppRoomProduto

Este guia acompanha os 8 itens da Parte A. Os arquivos `docs/prints/*.png` são inicialmente **marcadores ilustrativos**, não evidências de execução. Substitua cada marcador por um print verdadeiro, tirado no Android Studio ou celular, mantendo o mesmo nome.

## Preparação

- Execute no emulador API 24+ (use SDK 35 para compilar).
- No Android Studio, abra o **Logcat**, escolha o processo do app e filtre por `tag:TAG`.
- Depois de testar a exclusão total, cadastre novamente produtos para continuar.

| Arquivo | O que deve aparecer no print verdadeiro |
| --- | --- |
| `tela-inicial.png` | Tela inicial do aplicativo no emulador. |
| `logcat.png` | Logcat com produtos registrados e filtro `tag:TAG`. |
| `a1.png` | Inserir `Teclado` / `149,90` / `Teclado USB` / `3` e mostrar campos limpos, cursor em Nome. |
| `a2.png` | Tentar cadastrar com preço `0` e ver o erro **Informe um preço válido**. Repetir com vazio e negativo. |
| `a3.png` | Cadastrar Teclado e Mouse; clicar em **Listar Produtos** e ver os itens no TextView. |
| `a4.png` | Com Teclado (149,90, 3) e Mouse (59,00, 2), mostrar 2 produtos, soma dos preços de R$ 208,90, 5 unidades e valor em estoque de R$ 567,70. |
| `a5.png` | Buscar `tec` e mostrar apenas Teclado. |
| `a6.png` | Mostrar caixa de confirmação da exclusão de todos. Pode também registrar exclusão por ID. |
| `a7.png` | Atualizar preço de um item usando seu ID e mostrar novo valor na lista. |
| `a8.png` | Mostrar campo e valores de **Quantidade em estoque**. |

## Verificação da persistência

1. Insira dois produtos válidos e confira a lista.
2. Feche o aplicativo (ou interrompa o processo) e abra novamente.
3. Clique em **Listar Produtos** e confirme que os registros continuam lá.
4. Não desinstale nem limpe os dados durante o teste: isso excluiria o banco local.

## Entendendo a atualização da versão 1 para 2

Este projeto novo nasce já com banco **versão 2** e coluna `quantity`. Para testar uma migração, seria necessário instalar primeiro a versão 1, gravar produtos, depois instalar esta versão 2 sobre a anterior sem limpar os dados. O uso de `fallbackToDestructiveMigration()` sem `Migration(1, 2)` faz o Room recriar a tabela e apagar os cadastros antigos. Esse comportamento não ocorre só porque você fecha e abre o app.

## Tirando os prints

Clique em **Take Screenshot** na barra lateral do Android Emulator e substitua cada arquivo correspondente em `docs/prints/`. O print do Logcat deve ser tirado da janela do Android Studio.

## Totais esperados do teste

- Teclado: R$ 149,90 × 3 = R$ 449,70.
- Mouse: R$ 59,00 × 2 = R$ 118,00.
- Soma dos preços: **R$ 208,90**.
- Soma das quantidades: **5**.
- Valor total em estoque: **R$ 567,70**.

# 🚀 [WTC Sync] - Android

Aplicativo móvel desenvolvido nativamente para Android, focado em alta manutenibilidade e escalabilidade, seguindo as diretrizes de arquitetura moderna do Google.

## ✨ Tecnologias Principais

Este projeto foi construído utilizando o stack mais recente do Android:

* **Linguagem:** Kotlin
* **UI Toolkit:** Jetpack Compose (Interface Declarativa)
* **Arquitetura:** MVVM (Model-View-ViewModel)
* **Gestão de Estado:** ViewModel 
* **Dados/Backend:** Firebase (como fonte de dados remota)

## 🏗️ Estrutura de Pastas (Clean/Modular)

O projeto segue uma estrutura de pacotes orientada pela **Separação de Preocupações**, o que facilita a localização e o desenvolvimento de novas funcionalidades:

* `data/`: Contém a lógica de acesso aos dados (classes **Firebase**, **Modelos** e **Repository**).
* `ui/`: Camada de apresentação, dividida em módulos por funcionalidade (`crm`, `login`, `messages`). Contém as funções `@Composable`.
* `viewmodel/`: Responsável por coordenar a lógica de negócio e expor o estado (`State`) para a UI.
* `theme/`: Arquivos de configuração de **Jetpack Compose Theming** (cores, tipografia, etc.).

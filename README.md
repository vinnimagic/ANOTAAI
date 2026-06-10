# AnotAI - Notas Inteligentes com IA 📝🤖

O **AnotAI** é um aplicativo Android moderno para gestão de notas que utiliza Inteligência Artificial para transcrever áudios em texto, além de suportar fotos e anotações tradicionais.

## 🚀 Tecnologias Utilizadas

- **Linguagem:** Kotlin
- **Arquitetura:** MVVM (Model-View-ViewModel) com Clean Architecture
- **UI:** Jetpack Compose com Material Design 3
- **Injeção de Dependências:** Hilt (Dagger)
- **Banco de Dados:** Room Database (Persistência Local)
- **Rede/API:** OkHttp3 & Groq Cloud API (Modelo Whisper-large-v3)
- **Multimídia:** CameraX e MediaRecorder
- **Navegação:** Jetpack Navigation com Type Safety

## 🏗️ Arquitetura (MVVM + Repository)

O projeto segue os princípios de separação de responsabilidades:
- **UI Layer:** Composables que reagem ao estado do `ViewModel`.
- **ViewModel:** Gerencia o estado da UI (`UiState`) e interage com o Repositório.
- **Repository:** Centraliza o acesso aos dados, decidindo entre a API de Transcrição e o Banco de Dados local.
- **Data Layer (Room):** DAOs e Entidades para persistência de dados offline.

## ✨ Funcionalidades

- [x] **Autenticação:** Tela de login segura.
- [x] **Notas de Texto:** Criação e edição de anotações.
- [x] **Notas de Imagem:** Captura de fotos via câmera integradas à nota.
- [x] **Notas de Áudio:** Gravação de voz com player integrado.
- [x] **Transcrição via IA:** Conversão de áudio para texto em segundos usando a API Groq.
- [x] **Persistência:** Suas notas ficam salvas mesmo sem internet.

## 🛠️ Como Rodar o Projeto

1. Clone o repositório.
2. Certifique-se de ter o **Android Studio Ladybug** ou superior.
3. O projeto já inclui uma chave de API para testes, mas você pode configurar a sua no arquivo `NoteViewModel.kt`.
4. Sincronize o Gradle e rode o app em um dispositivo real ou emulador com suporte a áudio.

---
*Desenvolvido como projeto prático focando em boas práticas de desenvolvimento Android.*

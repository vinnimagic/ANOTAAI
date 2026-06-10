# Relatório Técnico - Projeto AnotAI 📄

Este documento detalha as decisões técnicas, arquitetura e implementação do aplicativo **AnotAI**.

## 1. Arquitetura (MVVM + Repository)
O projeto utiliza o padrão de projeto **MVVM (Model-View-ViewModel)**, que separa a lógica de negócios da interface do usuário:

- **Model:** Representa os dados (Room Entities e Modelos de domínio como `Note`, `NoteItemText`, etc).
- **View (Jetpack Compose):** Camada de interface reativa que observa o estado (`UiState`) exposto pelo ViewModel.
- **ViewModel:** Gerencia o estado da tela, lida com eventos do usuário e se comunica com o repositório.
- **Repository:** Atua como o "Single Source of Truth", mediando o acesso entre o banco de dados local (Room) e a API remota (Groq).

## 2. Persistência de Dados (Room)
Utilizamos o **Room Database** para garantir que o usuário possa acessar suas notas offline.
- **Relacionamentos:** Implementamos uma estrutura onde uma nota principal (`NoteEntity`) possui múltiplos itens (`TextNotes`, `AudioNotes`, `ImageNotes`) vinculados por um ID comum.
- **Transações:** O uso de `database.withTransaction` garante que operações complexas (como salvar uma nota e todos os seus itens) sejam atômicas, evitando corrupção de dados.

## 3. Integração com IA (API Groq - Whisper)
A transcrição de áudio é realizada via integração com a **Groq Cloud API**.
- **Modelo:** `whisper-large-v3`, um dos modelos de transcrição mais precisos do mundo.
- **Comunicação:** Implementada com `OkHttp3`, enviando o arquivo de áudio via `MultipartBody`.
- **Tratamento de Erros:** O sistema captura códigos HTTP (400, 401, 429) e fornece mensagens amigáveis ao usuário via Toasts.

## 4. Multimídia e Permissões
- **Áudio:** Gravação em formato MPEG_4 com codec AAC para alta fidelidade e baixo consumo de armazenamento.
- **Câmera:** Utilização da biblioteca **CameraX** para captura de imagens de alta performance.
- **Permissões:** Sistema de solicitação de permissões em tempo de execução para Microfone, Câmera e Armazenamento, seguindo as diretrizes do Android 13+.

## 5. UI/UX (Material Design 3)
O app segue o **Material Design 3 (Material You)**:
- **Componentes:** `Scaffold`, `TopAppBar`, `BottomAppBar` e `Card`.
- **Interatividade:** Feedback tátil (clique longo para excluir) e visual (ícones intuitivos e indicadores de carregamento).

---
*Este projeto foi desenvolvido com foco em escalabilidade, manutenibilidade e experiência do usuário.*

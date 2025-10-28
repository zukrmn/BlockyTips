# BlockyTips

BlockyTips é um plugin para o servidor BlockyCRAFT que envia dicas automáticas e configuráveis no chat do servidor, ajudando os jogadores com sugestões, informações e lembretes importantes sobre o servidor.

## Funcionalidades

- Envio automático de dicas para todos os jogadores online através do chat.
- Dicas configuráveis via arquivo `tips.properties`.
- Intervalo de exibição totalmente ajustável.
- Ciclo automático: após a última dica exibida, retorna ao início da lista.
- Comandos `/dicas on` e `/dicas off` para ativar/desativar dicas por jogador.
- Preferências persistidas entre reinícios do servidor.
- Dicas exibidas com cores e destaque, seguidas do aviso para desabilitação:  
  `§eUtilize §c/dicas off §epara desabilitar as dicas.`
- Compatível com Uberbukkit/CraftBukkit 1060 (Java 8).

## Configuração

O arquivo `tips.properties` permite definir:
- Todas as dicas do servidor (formato `tip.X`).
- O intervalo de exibição (`interval`).

**Exemplo:**
Intervalo entre dicas (em segundos)
`interval=1800`

**Dicas listadas**
tip.1=§eDica: ...
tip.2=§eOutra dica...


## Arquitetura

- **BlockyTips.java**: classe principal, responsável pelo ciclo do plugin, leitura de configuração, envio de dicas e controle dos comandos.
- **resources/**: contém os arquivos de configuração (plugin.yml e tips.properties).
- **disabled.yml**: arquivo gerado automaticamente para persistir quais jogadores desativaram as dicas.

## Comandos

- `/dicas on`: Ativa o recebimento de dicas automáticas.
- `/dicas off`: Desativa o recebimento de dicas automáticas.

## Reportar bugs ou requisitar features

Reporte bugs ou sugestões na seção [Issues](https://github.com/andradecore/BlockyTips/issues) do projeto. do projeto.

## Contato

- Discord: https://discord.gg/tthPMHrP
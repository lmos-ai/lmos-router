<!--
SPDX-FileCopyrightText: 2023 www.contributor-covenant.org

SPDX-License-Identifier: CC-BY-4.0
-->
# LLM Submodule

## Overview

The **LLM Submodule** is responsible for resolving agent routing specifications using a language model. It includes classes and interfaces for interacting with the OpenAI API by default, providing prompts to the model, and resolving agent routing specifications based on the model's responses. Additionally, it supports multiple language model providers such as Anthropic, Gemini, Ollama, and other OpenAI-compatible APIs through the LangChain4j integration.

## Table of Contents

1. [Introduction](#introduction)
2. [Classes and Interfaces](#classes-and-interfaces)
    - [ModelClient](#modelclient)
    - [DefaultModelClient](#defaultmodelclient)
    - [DefaultModelClientProperties](#defaultmodelclientproperties)
    - [LLMAgentRoutingSpecsResolver](#llmagentroutingspecsresolver)
    - [ModelPromptProvider](#modelpromptprovider)
    - [DefaultModelPromptProvider](#defaultmodelpromptprovider)
    - [ExternalModelPromptProvider](#externalmodelpromptprovider)
    - [AgentRoutingSpecListType](#agentroutingspeclisttype)
    - [ModelClientResponse](#modelclientresponse)
    - [LangChainModelClient](#langchainmodelclient)
    - [LangChainChatModelFactory](#langchainchatmodelfactory)
    - [LangChainClientProvider](#langchainclientprovider)
3. [Usage](#usage)
    - [Step 1: Initialize the DefaultModelClient](#step-1-initialize-the-defaultmodelclient)
    - [Step 2: Initialize the LLMAgentRoutingSpecsResolver](#step-2-initialize-the-llmagentroutingspecsresolver)
    - [Step 3: Resolve the Agent](#step-3-resolve-the-agent)
    - [Advanced: Using LangChainModelClient](#advanced-using-langchainmodelclient)
4. [Configuration](#configuration)
5. [Error Handling](#error-handling)
6. [License](#license)

## Introduction

The **LLM Submodule** leverages advanced language models to understand and match user queries with agent capabilities. It includes a default implementation for calling the OpenAI model and resolving agent routing specifications using the model's responses. Through the integration with **LangChain4j**, the submodule extends support to additional providers such as Anthropic, Gemini, Ollama, and other OpenAI-compatible APIs, offering flexibility in choosing the underlying language model service.

## Classes and Interfaces

### ModelClient

The `ModelClient` interface represents a client that can communicate with a language model.

- **Method:**
    - `call(messages: List<ChatMessage>): Result<ChatMessage, AgentRoutingSpecResolverException>`

### DefaultModelClient

The `DefaultModelClient` class is the default implementation of the `ModelClient` interface. It interacts with the OpenAI API to process messages.

- **Constructor:**
    - `DefaultModelClient(defaultModelClientProperties: DefaultModelClientProperties)`

- **Method:**
    - `call(messages: List<ChatMessage>): Result<ChatMessage, AgentRoutingSpecResolverException>`

### DefaultModelClientProperties

The `DefaultModelClientProperties` data class encapsulates the configuration properties required by the `DefaultModelClient`.

- **Fields:**
    - `openAiUrl: String`
    - `openAiApiKey: String`
    - `model: String`
    - `maxTokens: Int`
    - `temperature: Double`
    - `format: String`

### LLMAgentRoutingSpecsResolver

The `LLMAgentRoutingSpecsResolver` class is responsible for resolving agent routing specifications using a language model.

- **Constructor:**
    - `LLMAgentRoutingSpecsResolver(agentRoutingSpecsProvider: AgentRoutingSpecsProvider, modelPromptProvider: ModelPromptProvider, modelClient: ModelClient, serializer: Json, modelClientResponseProcessor: ModelClientResponseProcessor)`

- **Methods:**
    - `resolve(context: Context, input: UserMessage): Result<AgentRoutingSpec?, AgentRoutingSpecResolverException>`
    - `resolve(filters: Set<SpecFilter>, context: Context, input: UserMessage): Result<AgentRoutingSpec?, AgentRoutingSpecResolverException>`

### ModelPromptProvider

The `ModelPromptProvider` interface defines a provider that generates prompts for the language model based on the context and user input.

- **Method:**
    - `providePrompt(context: Context, agentRoutingSpecs: Set<AgentRoutingSpec>, input: UserMessage): Result<String, AgentRoutingSpecResolverException>`

### DefaultModelPromptProvider

The `DefaultModelPromptProvider` class offers a generic implementation of the `ModelPromptProvider`, generating standard prompts for agent resolution.

- **Method:**
    - `providePrompt(context: Context, agentRoutingSpecs: Set<AgentRoutingSpec>, input: UserMessage): Result<String, AgentRoutingSpecResolverException>`

### ExternalModelPromptProvider

The `ExternalModelPromptProvider` class generates prompts from an external file, supporting agent routing specifications in XML or JSON formats.

- **Constructor:**
    - `ExternalModelPromptProvider(promptFilePath: String, agentRoutingSpecsListType: AgentRoutingSpecListType)`

- **Method:**
    - `providePrompt(context: Context, agentRoutingSpecs: Set<AgentRoutingSpec>, input: UserMessage): Result<String, AgentRoutingSpecResolverException>`

### AgentRoutingSpecListType

The `AgentRoutingSpecListType` enum defines the supported formats for agent routing specifications.

- **Values:**
    - `XML`
    - `JSON`

### ModelClientResponse

The `ModelClientResponse` class encapsulates the response from the language model client.

- **Field:**
    - `agentName: String`

### LangChainModelClient

The `LangChainModelClient` class is an advanced implementation of the `ModelClient` interface using **LangChain4j** to interact with various language models.

- **Constructor:**
    - `LangChainModelClient(chatLanguageModel: ChatLanguageModel)`

- **Method:**
    - `call(messages: List<ChatMessage>): Result<ChatMessage, AgentRoutingSpecResolverException>`

**Details:**
- Converts internal `ChatMessage` types (`UserMessage`, `AssistantMessage`, `SystemMessage`) to **LangChain4j** compatible message types.
- Handles exceptions by encapsulating them within `AgentRoutingSpecResolverException`.

### LangChainChatModelFactory

The `LangChainChatModelFactory` is a factory class responsible for creating instances of `ChatLanguageModel` based on the provided configuration.

- **Companion Object Method:**
    - `createClient(properties: ModelClientProperties): ChatLanguageModel`

**Supported Providers:**
- `OPENAI`
- `ANTHROPIC`
- `GEMINI`
- `OLLAMA`
- `OTHER` (for OpenAI-compatible APIs)

**Details:**
- Configures the language model client with appropriate settings such as API keys, model names, token limits, temperature, and response formats based on the selected provider.

### LangChainClientProvider

The `LangChainClientProvider` enum lists the supported language model providers.

- **Values:**
    - `OPENAI`
    - `ANTHROPIC`
    - `GEMINI`
    - `OLLAMA`
    - `OTHER`

## Usage

### Step 1: Initialize the DefaultModelClient

```kotlin
val defaultModelClientProperties = DefaultModelClientProperties(
    openAiUrl = "https://api.openai.com/v1/chat/completions",
    openAiApiKey = "your-openai-api-key",
    model = "gpt-4",
    maxTokens = 1500,
    temperature = 0.7,
    format = "json"
)
val modelClient = DefaultModelClient(defaultModelClientProperties)
```

### Step 2: Initialize the LLMAgentRoutingSpecsResolver

```kotlin
val agentRoutingSpecsProvider = SimpleAgentRoutingSpecProvider()
val modelPromptProvider = DefaultModelPromptProvider()
val modelClientResponseProcessor = DefaultModelClientResponseProcessor()
val serializer = Json { ignoreUnknownKeys = true }

val llmAgentRoutingSpecsResolver = LLMAgentRoutingSpecsResolver(
    agentRoutingSpecsProvider,
    modelPromptProvider,
    modelClient,
    serializer,
    modelClientResponseProcessor
)
```

### Step 3: Resolve the Agent

```kotlin
val context = Context(listOf(AssistantMessage("Hello")))
val input = UserMessage("Can you help me find a new phone?")
val result = llmAgentRoutingSpecsResolver.resolve(context, input)
```

### Advanced: Using LangChainModelClient

For enhanced flexibility and support for multiple language model providers, you can utilize the `LangChainModelClient` along with the `LangChainChatModelFactory`.

```kotlin
// Define model client properties
val langChainProperties = ModelClientProperties(
    provider = LangChainClientProvider.OPENAI.name.lowercase(),
    apiKey = "your-openai-api-key",
    model = "gpt-4",
    maxTokens = 1500,
    temperature = 0.7,
    topP = 0.9,
    topK = 40,
    format = "json",
    baseUrl = null // Required for OTHER provider
)

// Create ChatLanguageModel using the factory
val chatLanguageModel = LangChainChatModelFactory.createClient(langChainProperties)

// Initialize LangChainModelClient
val langChainModelClient = LangChainModelClient(chatLanguageModel)

// Use LangChainModelClient with LLMAgentRoutingSpecsResolver
val llmAgentRoutingSpecsResolverAdvanced = LLMAgentRoutingSpecsResolver(
    agentRoutingSpecsProvider,
    modelPromptProvider,
    langChainModelClient,
    serializer,
    modelClientResponseProcessor
)

// Resolve agent as before
val advancedResult = llmAgentRoutingSpecsResolverAdvanced.resolve(context, input)
```

**Supported Providers via LangChainModelClient:**
- **OpenAI:** Default provider with full configuration support.
- **Anthropic:** Supports models from Anthropic.
- **Gemini:** Integrates with Google AI Gemini models.
- **Ollama:** Connects to Ollama-based models.
- **Other:** For any OpenAI-compatible API by specifying the `baseUrl`.

## Configuration

Configure the `DefaultModelClientProperties` or `ModelClientProperties` based on the chosen provider. Ensure that all required fields such as `apiKey`, `model`, and `baseUrl` (if applicable) are correctly set.

**Example Configuration for OpenAI:**

```kotlin
val properties = ModelClientProperties(
    provider = LangChainClientProvider.OPENAI.name.lowercase(),
    apiKey = "your-openai-api-key",
    model = "gpt-4",
    maxTokens = 1500,
    temperature = 0.7,
    topP = 0.9,
    topK = 40,
    format = "json",
    baseUrl = null
)
```

**Example Configuration for Other Providers:**

```kotlin
val properties = ModelClientProperties(
    provider = LangChainClientProvider.ANTHROPIC.name.lowercase(),
    apiKey = "your-anthropic-api-key",
    model = "claude-3-5-sonnet-20241022",
    maxTokens = 1500,
    temperature = 0.7
)
```

## Error Handling

The submodule employs error handling mechanisms to manage failures during model interactions and agent resolution.

- **Exceptions:**
  - `AgentRoutingSpecResolverException`: Thrown when there are issues in resolving agent routing specifications or interacting with the language model.

- **Handling Strategy:**
  - Utilize the `Result` type to handle successes and failures gracefully.
  - Implement appropriate fallback mechanisms or user notifications in case of failures.

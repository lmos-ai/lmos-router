// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.core

/**
 * Represents the context of a conversation.
 *
 * The previousMessages field contains the messages that have been exchanged in the conversation so far.
 */
open class Context(
    val previousMessages: List<ChatMessage>,
)

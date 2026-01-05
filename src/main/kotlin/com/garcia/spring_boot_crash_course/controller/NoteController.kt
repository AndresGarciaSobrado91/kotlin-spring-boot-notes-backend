package com.garcia.spring_boot_crash_course.controller

import com.garcia.spring_boot_crash_course.database.model.Note
import com.garcia.spring_boot_crash_course.database.repository.NoteRepository
import jakarta.validation.constraints.NotBlank
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@RestController
@RequestMapping("/notes")
class NoteController(
    private val noteRepository: NoteRepository
) {

    data class NoteRequest(
        val id: String?,
        @field:NotBlank(message = "Title must not be blank")
        val title: String,
        val content: String,
        val color: Long
    )

    data class NoteResponse(
        val id: String,
        val title: String,
        val content: String,
        val color: Long,
        val createdAt: Instant
    )

    @PostMapping
    fun save(
        @AuthenticationPrincipal ownerId: String, //Spring injects this automatically from the authenticated principal (JwtAuthFilter)
        @RequestBody body: NoteRequest
    ): NoteResponse {
        // val ownerId = SecurityContextHolder.getContext().authentication.principal as String // Alternative way to get the ownerId
        val note = noteRepository.save(
            Note(
                ownerId = ObjectId(ownerId),
                title = body.title,
                content = body.content,
                color = body.color,
                createdAt = Instant.now()
            )
        )

        return note.toResponse()
    }

    @GetMapping
    fun findByOwnerId(
        @AuthenticationPrincipal ownerId: String,
    ): List<NoteResponse> {
        val notes = noteRepository.findByOwnerId(ObjectId(ownerId))

        return notes.map { note ->
            note.toResponse()
        }
    }

    @DeleteMapping(path = ["/{id}"])
    fun deleteById(
        @AuthenticationPrincipal ownerId: String,
        @PathVariable id: String
    ) {
        val note =
            noteRepository.findById(ObjectId(id)).orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found")
            }

        if (note.ownerId.toHexString() != ownerId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to delete this note")
        }
        noteRepository.deleteById(ObjectId(id))
    }
}

private fun Note.toResponse() = NoteController.NoteResponse(
    id = this.id.toHexString(),
    title = this.title,
    content = this.content,
    color = this.color,
    createdAt = this.createdAt
)
package com.quantumsoft.tia.scanner.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl

class PageJacksonModule : SimpleModule() {
    init {
        val pageSerializer = PageSerializer()
        addSerializer(Page::class.java, pageSerializer)
        addSerializer(PageImpl::class.java, pageSerializer)
    }
}

class PageSerializer : JsonSerializer<Page<*>>() {
    override fun serialize(page: Page<*>, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeStartObject()
        gen.writeObjectField("content", page.content)
        gen.writeNumberField("totalElements", page.totalElements)
        gen.writeNumberField("totalPages", page.totalPages)
        gen.writeNumberField("size", page.size)
        gen.writeNumberField("number", page.number)
        gen.writeBooleanField("first", page.isFirst)
        gen.writeBooleanField("last", page.isLast)
        gen.writeBooleanField("empty", page.isEmpty)
        
        // Write pageable
        gen.writeObjectFieldStart("pageable")
        gen.writeNumberField("pageNumber", page.pageable.pageNumber)
        gen.writeNumberField("pageSize", page.pageable.pageSize)
        gen.writeNumberField("offset", page.pageable.offset)
        gen.writeBooleanField("paged", page.pageable.isPaged)
        gen.writeBooleanField("unpaged", page.pageable.isUnpaged)
        
        // Write sort
        gen.writeObjectFieldStart("sort")
        gen.writeBooleanField("sorted", page.pageable.sort.isSorted)
        gen.writeBooleanField("unsorted", page.pageable.sort.isUnsorted)
        gen.writeBooleanField("empty", page.pageable.sort.isEmpty)
        gen.writeEndObject()
        
        gen.writeEndObject()
        
        // Write sort at page level
        gen.writeObjectFieldStart("sort")
        gen.writeBooleanField("sorted", page.sort.isSorted)
        gen.writeBooleanField("unsorted", page.sort.isUnsorted)
        gen.writeBooleanField("empty", page.sort.isEmpty)
        gen.writeEndObject()
        
        gen.writeNumberField("numberOfElements", page.numberOfElements)
        gen.writeEndObject()
    }
}

package org.opensearch.replication.metadata

import org.opensearch.action.ActionRequestValidationException
import org.opensearch.action.ValidateActions.addValidationError
import org.opensearch.action.admin.indices.alias.IndicesAliasesRequest
import org.opensearch.action.admin.indices.close.CloseIndexRequest
import org.opensearch.action.admin.indices.mapping.put.PutMappingRequest
import org.opensearch.action.admin.indices.open.OpenIndexRequest
import org.opensearch.action.admin.indices.settings.put.UpdateSettingsRequest
import org.opensearch.action.support.master.AcknowledgedRequest
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput

class UpdateMetadataRequest : AcknowledgedRequest<UpdateMetadataRequest> {
    var indexName: String
    var type: Type
    var request: AcknowledgedRequest<*>

    constructor(indexName: String, type: Type, request: AcknowledgedRequest<*>) : super() {
        this.indexName = indexName
        this.type = type
        this.request = request
    }

    enum class Type {
        MAPPING, SETTING, ALIAS, OPEN, CLOSE
    }

    constructor(inp: StreamInput): super(inp) {
        indexName = inp.readString()
        type = inp.readEnum(Type::class.java)
        request = when (type) {
            Type.MAPPING -> PutMappingRequest(inp)
            Type.SETTING -> UpdateSettingsRequest(inp)
            Type.ALIAS -> IndicesAliasesRequest(inp)
            Type.OPEN -> OpenIndexRequest(inp)
            Type.CLOSE -> CloseIndexRequest(inp)
        }
    }

    override fun validate(): ActionRequestValidationException? {
        var validationException: ActionRequestValidationException? = request.validate()
        if (indexName == null) validationException = addValidationError("index name is missing", validationException)
        if (type == null) validationException = addValidationError("operation types is missing", validationException)
        return validationException
    }

    override fun writeTo(out: StreamOutput) {
        super.writeTo(out)
        out.writeString(indexName)

        out.writeEnum(type)

        request.writeTo(out)
    }
}
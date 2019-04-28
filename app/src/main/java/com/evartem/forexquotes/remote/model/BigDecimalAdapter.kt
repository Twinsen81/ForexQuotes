package com.evartem.forexquotes.remote.model

import com.squareup.moshi.*
import java.math.BigDecimal

class BigDecimalAdapter: JsonAdapter<BigDecimal>() {

    @FromJson
    override fun fromJson(reader: JsonReader): BigDecimal? =
        if (reader.peek() == JsonReader.Token.NULL)
            BigDecimal.ZERO
        else
            BigDecimal(reader.nextString())

    @ToJson
    override fun toJson(writer: JsonWriter, value: BigDecimal?) {
        if (value == null)
            writer.value(0)
        else
            writer.value(value.toPlainString())
    }
}
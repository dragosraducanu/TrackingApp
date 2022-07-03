package com.dragos.challenge.data.source.db

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object TrackingDataSerializer: Serializer<TrackingData> {
    override val defaultValue: TrackingData = TrackingData.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): TrackingData {
        try {
            return TrackingData.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: TrackingData,
        output: OutputStream
    ) = t.writeTo(output)
}

val Context.dataStore: DataStore<TrackingData> by dataStore(
    fileName = "dataStore.pb",
    serializer = TrackingDataSerializer
)
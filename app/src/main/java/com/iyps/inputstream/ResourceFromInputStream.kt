package com.iyps.inputstream

import com.nulabinc.zxcvbn.io.Resource
import java.io.InputStream

class ResourceFromInputStream(private val inputStream: InputStream) : Resource {
    override fun getInputStream(): InputStream {
        return inputStream
    }
}
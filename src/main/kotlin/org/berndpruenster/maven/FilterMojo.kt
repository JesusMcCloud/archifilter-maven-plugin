/*
Copyright (c) 2016, 2017 Bernd Prünster
This file is part of of the unofficial Java-Tor-bindings.

Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the
European Commission - subsequent versions of the EUPL (the "Licence"); You may
not use this work except in compliance with the Licence. You may obtain a copy
of the Licence at: http://joinup.ec.europa.eu/software/page/eupl

Unless required by applicable law or agreed to in writing, software distributed
under the Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR
CONDITIONS OF ANY KIND, either express or implied. See the Licence for the
specific language governing permissions and limitations under the Licence.

This project includes components developed by third parties and provided under
various open source licenses (www.opensource.org).

*/
package org.berndpruenster.maven

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

@Mojo(name = "filter")
class FilterMojo : AbstractMojo() {


    @Parameter(required = true)
    lateinit var src: String
    @Parameter(required = true)
    lateinit var dst: String

    @Parameter(required = false, defaultValue = "tor-browser_en-US/Browser/TorBrowser/Tor/")
    lateinit var prefix: String

    @Throws(MojoExecutionException::class)
    override fun execute() {

        File(dst).parentFile.mkdirs()
        val destination = TarArchiveOutputStream(XZCompressorOutputStream(FileOutputStream(dst)))
        destination.use {
            if (src.endsWith(".tar.xz")) {
                val source = TarArchiveInputStream(XZCompressorInputStream(FileInputStream(src)))

                var entry = source.nextTarEntry
                while (entry != null) {
                    if (entry.name != prefix && entry.name.startsWith(prefix) && entry.isFile) {
                        entry.name = entry.name.substring(prefix.length)
                        log.info("repackaging ${entry.name}")

                        destination.putArchiveEntry(entry)
                        source.copyTo(destination)
                        destination.closeArchiveEntry()
                    }
                    entry = source.nextTarEntry
                }

            } else
                throw MojoExecutionException("only tar.xz is currently supported!")
        }
    }
}

package ydsu.module.manage.source

import com.google.common.collect.ImmutableSet
import com.google.common.io.Files

import java.util.function.Predicate

@Grab(group = 'com.google.guava', module = 'guava', version = '28.1-jre', transitive = false)
@Singleton
class ContainsRegularNonDocumentFile implements Predicate<File> {
    ImmutableSet<String> DOCUMENT_EXTENSIONS = ImmutableSet.of('ad', 'adoc', 'asciidoc', 'doc', 'docx', 'htm', 'html', 'md', 'odt', 'pdf', 'txt')

    /**
     * If the baseDir directory has at least one regular file then it is a single module. Otherwise, it's a collection
     * of modules. Hidden files like .DS_Store are NOT taken into account. Also, documentation files with any of the
     * {@link #DOCUMENT_EXTENSIONS} extension are NOT taken into account.
     *
     * @param baseDir the base directory of the module group
     * @return {@code true} if the module is single, {@code false} otherwise
     */
    @Override
    boolean test(File baseDir) {
        baseDir.listFiles({
            it.isFile() && !it.isHidden() && !DOCUMENT_EXTENSIONS.contains(Files.getFileExtension(it.name))
        } as FileFilter)
    }

    @Override
    String toString() {
        'ContainsRegularNonDocumentFile{}'
    }
}

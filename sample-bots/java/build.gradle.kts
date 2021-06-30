abstract class JavaSampleBotsTask : DefaultTask() {
    @TaskAction
    fun task() {
        val files = File(".").list()

        files.forEach { filename -> run {
            val file = File(filename)
            if (file.isDirectory && !filename.startsWith(".") && !filename.equals("gradle")) {
                val jarFilename = getJarArchiveName(file)
                if (jarFilename != null) {
                    println(jarFilename)
                }
            }
        }}
    }

    private fun runMavenBuild(dir: File) {
        println("Running Maven build for: $dir")

        var mvn = System.getenv("MAVEN_HOME") + "/bin/mvn"
        if (System.getProperty("os.name").startsWith("Windows")) {
           mvn += ".cmd"
        }
        val pb = ProcessBuilder(mvn, "clean", "package")
        pb.directory(File(System.getProperty("user.dir"), dir.name))
        pb.start()
    }

    private fun getJarArchiveName(dir: File): String? {
        val dir2 = File(System.getProperty("user.dir"), dir.name + "/target")
        dir2.list()?.forEach { filename ->
            if (filename.startsWith(dir.name)) {
                return filename
            }
        }
        System.err.println("Could not find jar archive in dir: $dir")
        return null
    }
}

tasks.register<JavaSampleBotsTask>("build")
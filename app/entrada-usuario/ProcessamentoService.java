package org.example.web.service;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.example.web.dto.ResultadoArquivo;
import org.example.web.dto.ResultadoProcessamento;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class ProcessamentoService {

    private Path baseWorkDir() throws IOException {
        Path base = Paths.get(System.getProperty("java.io.tmpdir"), "gerador-testes");
        Files.createDirectories(base);
        return base;
    }

    public ResultadoProcessamento processarTrechoCodigo(String codigo) throws IOException {
        String execId = UUID.randomUUID().toString();
        Path work = baseWorkDir().resolve(execId);
        Files.createDirectories(work);

        // salva como arquivo único
        Path javaFile = work.resolve("CodigoSubmetido_" + Instant.now().toEpochMilli() + ".java");
        Files.writeString(javaFile, codigo, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);

        ResultadoProcessamento res = new ResultadoProcessamento();
        res.setIdExecucao(execId);
        res.setTotalArquivos(1);
        res.setTotalJava(1);
        res.getArquivos().add(new ResultadoArquivo(javaFile.toString(), Files.size(javaFile), true, "Trecho salvo"));
        res.setProximaAcao("ANALISAR_COBERTURA_E_GERAR_TESTES");
        return res;
    }

    public ResultadoProcessamento processarArquivo(MultipartFile file) throws IOException {
        String execId = UUID.randomUUID().toString();
        Path work = baseWorkDir().resolve(execId);
        Files.createDirectories(work);

        Path destino = work.resolve(file.getOriginalFilename());
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, destino, StandardCopyOption.REPLACE_EXISTING);
        }

        boolean isJava = FilenameUtils.getExtension(destino.getFileName().toString()).equalsIgnoreCase("java");

        ResultadoProcessamento res = new ResultadoProcessamento();
        res.setIdExecucao(execId);
        res.setTotalArquivos(1);
        res.setTotalJava(isJava ? 1 : 0);
        res.getArquivos().add(new ResultadoArquivo(destino.toString(), Files.size(destino), isJava,
                isJava ? "Arquivo .java recebido" : "Arquivo recebido (não .java)"));
        res.setProximaAcao("ANALISAR_COBERTURA_E_GERAR_TESTES");
        return res;
    }

    // public ResultadoProcessamento processarProjetoZip(MultipartFile zip) throws IOException {
    //     String execId = UUID.randomUUID().toString();
    //     Path work = baseWorkDir().resolve(execId);
    //     Files.createDirectories(work);

    //     Path zipPath = work.resolve(zip.getOriginalFilename());
    //     try (InputStream in = zip.getInputStream()) {
    //         Files.copy(in, zipPath, StandardCopyOption.REPLACE_EXISTING);
    //     }

    //     // descompactar
    //     Path extracted = work.resolve("projeto");
    //     Files.createDirectories(extracted);
    //     unzip(zipPath, extracted);

    //     // listar arquivos
    //     List<File> files = FileUtils.listFiles(extracted.toFile(), null, true)
    //             .stream().toList();

    //     ResultadoProcessamento res = new ResultadoProcessamento();
    //     res.setIdExecucao(execId);
    //     res.setTotalArquivos(files.size());

    //     int totalJava = 0;
    //     for (File f : files) {
    //         boolean isJava = FilenameUtils.getExtension(f.getName()).equalsIgnoreCase("java");
    //         if (isJava) totalJava++;
    //         res.getArquivos().add(new ResultadoArquivo(f.getAbsolutePath(), f.length(), isJava, isJava ? "OK" : "Ignorado (não .java)"));
    //     }
    //     res.setTotalJava(totalJava);
    //     res.setProximaAcao("ANALISAR_COBERTURA_E_GERAR_TESTES");
    //     return res;
    // }

    public ResultadoProcessamento processarProjetoZip(MultipartFile zip) throws IOException {
        String execId = UUID.randomUUID().toString();
        Path work = baseWorkDir().resolve(execId);
        Files.createDirectories(work);

        // Pastas separadas
        Path extractedJava = work.resolve("java-files");
        Path extractedClass = work.resolve("class-files");
        Files.createDirectories(extractedJava);
        Files.createDirectories(extractedClass);

        try (ZipInputStream zis = new ZipInputStream(zip.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;

                String lowerName = entry.getName().toLowerCase();
                Path targetPath = null;

                if (lowerName.endsWith(".java")) {
                    targetPath = extractedJava.resolve(Paths.get(entry.getName()).getFileName());
                } else if (lowerName.endsWith(".class")) {
                    targetPath = extractedClass.resolve(Paths.get(entry.getName()).getFileName());
                }

                if (targetPath != null) {
                    Files.createDirectories(targetPath.getParent());
                    Files.copy(zis, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }

        // Lista arquivos
        List<File> javaFiles = FileUtils.listFiles(extractedJava.toFile(), new String[]{"java"}, true)
                .stream().toList();
        List<File> classFiles = FileUtils.listFiles(extractedClass.toFile(), new String[]{"class"}, true)
                .stream().toList();

        ResultadoProcessamento res = new ResultadoProcessamento();
        res.setIdExecucao(execId);
        res.setTotalArquivos(javaFiles.size() + classFiles.size());
        res.setTotalJava(javaFiles.size());

        // Adiciona detalhes
        javaFiles.forEach(f -> res.getArquivos().add(new ResultadoArquivo(
                f.getAbsolutePath(),
                f.length(),
                true,
                "Arquivo Java extraído"
        )));
        classFiles.forEach(f -> res.getArquivos().add(new ResultadoArquivo(
                f.getAbsolutePath(),
                f.length(),
                true,
                "Arquivo .class extraído"
        )));

        res.setProximaAcao("ANALISAR_COBERTURA_E_GERAR_TESTES");
        return res;
    }


    private void unzip(Path zip, Path target) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zip))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path outPath = target.resolve(entry.getName()).normalize();
                if (!outPath.startsWith(target)) {
                    throw new IOException("Zip Path Traversal detectado");
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(outPath);
                } else {
                    Files.createDirectories(outPath.getParent());
                    try (OutputStream os = Files.newOutputStream(outPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                        zis.transferTo(os);
                    }
                }
                zis.closeEntry();
            }
        }
    }
}

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Persistencia {
    public static void salvar(CentralResgate central, String nomeArquivo) throws IOException {
        if (central == null) {
            throw new IllegalArgumentException("Nao e possivel salvar uma central nula.");
        }
        File arquivo = new File(nomeArquivo);
        File pasta = arquivo.getParentFile();
        if (pasta != null && !pasta.exists() && !pasta.mkdirs()) {
            throw new IOException("Nao foi possivel criar a pasta de dados.");
        }
        try (ObjectOutputStream saida = new ObjectOutputStream(new FileOutputStream(arquivo))) {
            saida.writeObject(central);
        }
    }

    public static CentralResgate carregar(String nomeArquivo) throws IOException, ClassNotFoundException {
        try (ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(nomeArquivo))) {
            Object objeto = entrada.readObject();
            if (!(objeto instanceof CentralResgate)) {
                throw new IOException("O arquivo nao contem uma CentralResgate.");
            }
            return (CentralResgate) objeto;
        }
    }
}

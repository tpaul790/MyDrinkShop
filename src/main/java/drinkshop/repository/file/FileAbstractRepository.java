package drinkshop.repository.file;

import drinkshop.repository.AbstractRepository;

import java.io.*;

public abstract class FileAbstractRepository<ID, E>
        extends AbstractRepository<ID, E> {

    protected String fileName;

    public FileAbstractRepository(String fileName) {
        this.fileName = fileName;
        //loadFromFile();
    }

    protected void loadFromFile() {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {

            String line;
            int lineNo = 0;
            while ((line = br.readLine()) != null) {
                lineNo++;
                if (line.isBlank()) {
                    continue;
                }
                try {
                    E entity = extractEntity(line);
                    super.save(entity);
                } catch (RuntimeException ex) {
                    throw new IllegalStateException("Eroare la parsarea fisierului " + fileName + " la linia " + lineNo + ": " + line, ex);
                }
            }

        } catch (IOException e) {
            throw new IllegalStateException("Nu s-a putut citi fisierul: " + fileName, e);
        }
    }

    private void writeToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {

            for (E entity : entities.values()) {
                bw.write(createEntityAsString(entity));
                bw.newLine();
            }

        } catch (IOException e) {
            throw new IllegalStateException("Nu s-a putut scrie fisierul: " + fileName, e);
        }
    }

    @Override
    public E save(E entity) {
        E e = super.save(entity);
        writeToFile();
        return e;
    }

    @Override
    public E delete(ID id) {
        E e = super.delete(id);
        writeToFile();
        return e;
    }

    @Override
    public E update(E entity) {
        E e = super.update(entity);
        writeToFile();
        return e;
    }

    protected abstract E extractEntity(String line);

    protected abstract String createEntityAsString(E entity);
}

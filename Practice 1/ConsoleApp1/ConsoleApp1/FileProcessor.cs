namespace ConsoleApp1;

/// <summary>
/// Класс для работы с файлами.
/// </summary>
/// <param name="filePath">Путь к файлу.</param>
public class FileProcessor(string filePath)
{
    /// <summary>
    /// Метод для чтения содержимого файла.
    /// </summary>
    public string ReadFile()
    {
        string content;
        try
        {
            content = File.ReadAllText(filePath);
        }
        catch (IOException e)
        {
            Utils.ShowError("Не удалось прочитать файл!");
            return "";
        }
        return string.IsNullOrEmpty(content) ? "Файл пуст!" : content;
    }

    /// <summary>
    /// Метод для подсчета вхождений заданного слова в файл.
    /// </summary>
    /// <param name="word">Слово, для которого требуется подсчитать вхождения.</param>
    public int CountWord(string word)
    {
        try
        {
            string content = File.ReadAllText(filePath);
            //Получения массива всех слов в файле и подсчет количества слов равных заданному
            return content.Split([' ', '\n', '\r', '\t'], StringSplitOptions.RemoveEmptyEntries)
                .Count(x => x.Equals(word, StringComparison.InvariantCultureIgnoreCase));
        }
        catch (IOException e)
        {
            Utils.ShowError(e.Message);
            return -1;
        }
    }
        
    /// <summary>
    /// Метод для подсчета всех слов в файле.
    /// </summary>
    public int CountFileWords()
    {
        try
        {
            string content = File.ReadAllText(filePath);
            return content.Split([' ', '\n', '\r', '\t'], StringSplitOptions.RemoveEmptyEntries).Length;
        }
        catch (IOException e)
        {
            Utils.ShowError(e.Message);
            return -1;
        }
    }
}
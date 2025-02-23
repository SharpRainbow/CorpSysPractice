namespace TestProject1;
using ConsoleApp1;

/// <summary>
/// Класс для тестирования методов работы с файлом.
/// </summary>
public class Tests
{

    //Тестовые данные для записи в файл
    private const string Data =
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

    //Имя файла
    private const string FileName = "test.txt";
    
    //Эксземпляр класса обработчика файла
    private FileProcessor fp;
    
    /// <summary>
    /// Создание класса обработчика, создание файла и запись тестовых данных.
    /// </summary>
    [SetUp]
    public void Setup()
    {
        fp = new FileProcessor(FileName);
        if (File.Exists(FileName))
        {
            File.Delete(FileName);
        }
        File.Create(FileName).Close();
        File.WriteAllText(FileName, Data);
    }

    /// <summary>
    /// Проверка метода чтения файла.
    /// </summary>
    [Test]
    public void TestFileRead()
    {
        Assert.That(fp.ReadFile(), Is.EqualTo(Data));
    }

    /// <summary>
    /// Проверка метода подсчета вхождений слова в файле.
    /// </summary>
    [Test]
    public void TestWordSearch()
    {
        Assert.That(fp.CountWord("in"), Is.EqualTo(3));
    }

    /// <summary>
    /// Проверка метода подчеста количества слов в файле.
    /// </summary>
    [Test]
    public void TestWordCount()
    {
        Assert.That(fp.CountFileWords(), Is.EqualTo(Data.Split(' ').Length));
    }

    /// <summary>
    /// Удаление файла после завершения тестирования.
    /// </summary>
    [TearDown]
    public void Cleanup()
    {
        if (File.Exists(FileName))
        {
            File.Delete(FileName);
        }
    }
}
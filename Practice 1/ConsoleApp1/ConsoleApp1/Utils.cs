namespace ConsoleApp1;

/// <summary>
/// Класс для хранения вспомогательных методов.
/// </summary>
public class Utils
{
    /// <summary>
    /// Метод для отображения сообщений об ошибке.
    /// </summary>
    /// <param name="message">Сообщение об ошибке.</param>
    public static void ShowError(string message)
    {
        Console.WriteLine($"Ошибка: {message}");
    }

    /// <summary>
    /// Метод останавливающий выполнение программы и ожидающий ввода пользователя.
    /// </summary>
    /// <param name="message">Сообение для пользователя.</param>
    public static void AwaitInput(string message)
    {
        Console.Write(message);
        Console.ReadLine();
    }
}
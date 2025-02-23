namespace ConsoleApp1;

/// <summary>
/// Класс содержащий основную логику приложения.
/// </summary>
/// <param name="fileProcessor">Экземпляр класса для работы с файлами</param>
public class FilesApp(FileProcessor fileProcessor)
{
    private const string Menu = "Выберите операцию с файлом:\n1.Прочитать\n2.Найти слово\n3.Подсчитать количество слов в файле\n";

    public void Run()
    {
        //Бесконечный цикл отобрадащий меню и запрашивающий у пользователя выбор опции 
        //Для выхода из цикла можно использовать exit
        while (true)
        {
            //Очистка консоли и отображение меню
            Console.Clear();
            Console.WriteLine(Menu);
            string? input = Console.ReadLine();
            //Обработка выбранной опции
            switch (input)
            {
                case "1":
                {
                    Console.WriteLine(fileProcessor.ReadFile());
                    break;
                }
                case "2":
                {
                    Console.Write("Введите слово для поиска: ");
                    input = Console.ReadLine();
                    int count = fileProcessor.CountWord(input);
                    Console.WriteLine(count > 0 ? $"В файле содержится {count} повторений"
                        : "Заданное слово не найдено!");
                    break;
                }
                case "3":
                {
                    int count = fileProcessor.CountFileWords();
                    if (count > 0)
                    {
                        Console.WriteLine($"В файле содержится {count} слов");
                    }
                    break;
                }
                case "exit":
                    return;
                default:
                {
                    Utils.ShowError("Неизвестная операция");
                    break;
                }
            }
            //Ожидание ввода пользователя для продолжения работы цикла
            Utils.AwaitInput("Для продолжения нажмите Enter...");
        }
    }
}
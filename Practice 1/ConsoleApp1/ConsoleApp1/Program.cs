// See https://aka.ms/new-console-template for more information

namespace ConsoleApp1
{
    class Program
    {
        public static void Main(string[] args)
        {
            //Для начала работы с файлом запрашиваем у пользователя путь
            Console.Write("Введите путь к файлу для продолжения: ");
            string? filePath = Console.ReadLine();
            //Проверяем корректность введенного пути.
            if (!File.Exists(filePath))
            {
                //Если файл не существует или имя не было введено, возвращаем сообщение об ошибке
                Utils.ShowError("Файл не найден!");
                return;
            }
            FileProcessor fp = new FileProcessor(filePath);
            FilesApp app = new FilesApp(fp);
            //Запуск цикла работы приложения
            app.Run();
        }
    }
}

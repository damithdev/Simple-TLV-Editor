package dev.damith.simpletlv;

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.FileReader;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class EmvData {

    @CsvBindByPosition(position = 0)
    private String tag;

    @CsvBindByPosition(position = 1)
    private String name;

    @CsvBindByPosition(position = 2)
    private String description;

    @CsvBindByPosition(position = 3)
    private String source;

    @CsvBindByPosition(position = 4)
    private String format;

    @CsvBindByPosition(position = 5)
    private String template;

    @CsvBindByPosition(position = 6)
    private String minLength;

    @CsvBindByPosition(position = 7)
    private String maxLength;

    @CsvBindByPosition(position = 8)
    private String pc;
    // Primitive or Constructed


    @CsvBindByPosition(position = 9)
    private String example;


    public static void init() {

        try{
            String file = CustomFileReader.getFileFromResources("emvtagssunmi.csv");

            List<EmvData> emvData = new CsvToBeanBuilder<EmvData>(new FileReader(file)).withType(EmvData.class).build().parse();

            emvData.forEach(x->{
                StaticEntry.emvTags.put(x.getTag(),x);
            });

            if(emvData.size() == StaticEntry.emvTags.size()){
                System.out.println("EMV Data Successfully Loaded");
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}

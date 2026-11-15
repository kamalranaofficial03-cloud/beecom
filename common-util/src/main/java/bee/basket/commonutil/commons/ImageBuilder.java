package bee.basket.commonutil.commons;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ImageBuilder {
    private final String separator;

    public List<String> split(String input){
        return Arrays.asList(input.split(Pattern.quote(this.separator),-1));
    }
    public String join(List<String> images){
        Objects.requireNonNull(images, "Images cannot be null");
        return images.stream().collect(Collectors.joining(this.separator));
    }

    private ImageBuilder(String separator){
        this.separator = separator;
    }

    public static ImageBuilder.Builder builder(){
        return new ImageBuilder.Builder();
    }

    public static final class Builder{
        private String separator;
        Builder(){
        }

        public Builder sep(String separator){
            this.separator = separator;
            return this;
        }
        public ImageBuilder build(){
            Objects.requireNonNull(separator, "Separator cannot be null");
            if (separator.isEmpty()) {
                throw new IllegalArgumentException(
                        "Separator cannot be empty"
                );
            }
            return new ImageBuilder(this.separator);
        }
    }
}

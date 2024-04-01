
# just displays the recipes
_default:
    @just --list

clean:
    ./gradlew clean

build:
    ./gradlew build

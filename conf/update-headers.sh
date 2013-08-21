header="/home/peter/gitrepos/podd-redesign/conf/LicenseHeaderJava.txt"
for file in "$@"
do
    cat "$header" "$file" > /tmp/xx.$$
    mv /tmp/xx.$$ "$file"
done

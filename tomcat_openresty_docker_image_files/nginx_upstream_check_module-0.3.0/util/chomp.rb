STDIN.each do |line|
  next unless line
  res = line.gsub(/\s+$/, "")
  puts "#{res}"
end

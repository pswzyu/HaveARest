function [ grav, acc ] = loadRecordedPoseSequence( filename )
%LOADRECORDEDPOSESEQUENCE Summary of this function goes here
%   Detailed explanation goes here
% load the recorded poses from file

gravity = [];
linear_acc = [];

fid = fopen(filename); % open the file
tline = fgetl(fid); % read one line
while ischar(tline) % if we haven't reach eof
    if strfind(tline, 'TESTtrue')
        % ignore this line
    end
    if strfind(tline, 'LinearAcc')
        nums = strsplit(tline(45:end), ',');
        linear_acc = [linear_acc; ...
            str2double(nums{1}),str2double(nums{2}),str2double(nums{3})];
    end
    if strfind(tline, 'Gravity')
        nums = strsplit(tline(43:end), ',');
        gravity = [gravity; ...
            str2double(nums{1}),str2double(nums{2}),str2double(nums{3})];
    end
    
    tline = fgetl(fid); % read another line
end
fclose(fid);

grav = gravity;
acc = linear_acc;
%output_args = [gravity, linear_acc];

end